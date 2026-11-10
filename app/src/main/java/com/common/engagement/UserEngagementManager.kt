package com.common.engagement

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.engagementDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_engagement_preferences")

data class UserEngagementMetrics(
    val installTimestamp: Long = 0L,
    val sessionCount: Int = 0,
    val manualRefreshCount: Int = 0,
    val taskCompletionCount: Int = 0,
    val lastPromptTimestamp: Long = 0L,
    val isRated: Boolean = false,
    val lastRatingPromptTime: Long = 0L,
    val ratingPromptCount: Int = 0,
    val isShared: Boolean = false,
    val lastSharePromptTime: Long = 0L,
    val sharePromptCount: Int = 0,
    val lastUpdatePromptTime: Long = 0L
)

class UserEngagementManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        val KEY_INSTALL_TIME = longPreferencesKey("key_install_time")
        val KEY_SESSION_COUNT = intPreferencesKey("key_session_count")
        val KEY_MANUAL_REFRESH_COUNT = intPreferencesKey("key_manual_refresh_count")
        val KEY_TASK_COMPLETION_COUNT = intPreferencesKey("key_task_completion_count")
        val KEY_LAST_ANY_PROMPT_TIME = longPreferencesKey("key_last_any_prompt_time")
        val KEY_IS_RATED = booleanPreferencesKey("key_is_rated")
        val KEY_LAST_RATING_PROMPT_TIME = longPreferencesKey("key_last_rating_prompt_time")
        val KEY_RATING_PROMPT_COUNT = intPreferencesKey("key_rating_prompt_count")
        val KEY_IS_SHARED = booleanPreferencesKey("key_is_shared")
        val KEY_LAST_SHARE_PROMPT_TIME = longPreferencesKey("key_last_share_prompt_time")
        val KEY_SHARE_PROMPT_COUNT = intPreferencesKey("key_share_prompt_count")
        val KEY_LAST_UPDATE_PROMPT_TIME = longPreferencesKey("key_last_update_prompt_time")

        // 30-day minimum cooldown and quality gate thresholds
        const val COOLDOWN_30_DAYS_MS = 30L * 24 * 60 * 60 * 1000L // 30 Days minimum cooldown
        const val RATING_MIN_DAYS_INSTALLED = 3L // 3 days post-install
        const val RATING_MIN_MANUAL_REFRESHES = 5 // 5th manual refresh threshold
        const val RATING_MIN_SESSIONS = 5 // 5 sessions threshold
        const val SHARE_MIN_TASKS = 5 // 5 tasks completion threshold
        const val MAX_RATING_PROMPT_ATTEMPTS = 3
        const val MAX_SHARE_PROMPT_ATTEMPTS = 2
    }

    private val dataStore = context.engagementDataStore

    val metricsFlow: Flow<UserEngagementMetrics> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserEngagementMetrics(
                installTimestamp = preferences[KEY_INSTALL_TIME] ?: 0L,
                sessionCount = preferences[KEY_SESSION_COUNT] ?: 0,
                manualRefreshCount = preferences[KEY_MANUAL_REFRESH_COUNT] ?: 0,
                taskCompletionCount = preferences[KEY_TASK_COMPLETION_COUNT] ?: 0,
                lastPromptTimestamp = preferences[KEY_LAST_ANY_PROMPT_TIME] ?: 0L,
                isRated = preferences[KEY_IS_RATED] ?: false,
                lastRatingPromptTime = preferences[KEY_LAST_RATING_PROMPT_TIME] ?: 0L,
                ratingPromptCount = preferences[KEY_RATING_PROMPT_COUNT] ?: 0,
                isShared = preferences[KEY_IS_SHARED] ?: false,
                lastSharePromptTime = preferences[KEY_LAST_SHARE_PROMPT_TIME] ?: 0L,
                sharePromptCount = preferences[KEY_SHARE_PROMPT_COUNT] ?: 0,
                lastUpdatePromptTime = preferences[KEY_LAST_UPDATE_PROMPT_TIME] ?: 0L
            )
        }

    private val _currentPrompt = MutableStateFlow(PromptType.NONE)
    val currentPrompt: StateFlow<PromptType> = _currentPrompt.asStateFlow()

    init {
        scope.launch {
            dataStore.edit { preferences ->
                if (preferences[KEY_INSTALL_TIME] == null) {
                    preferences[KEY_INSTALL_TIME] = System.currentTimeMillis()
                }
                val currentSessions = preferences[KEY_SESSION_COUNT] ?: 0
                preferences[KEY_SESSION_COUNT] = currentSessions + 1
            }
        }
    }

    /**
     * Record a manual refresh action in DataStore
     */
    fun recordManualRefresh() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[KEY_MANUAL_REFRESH_COUNT] ?: 0
                preferences[KEY_MANUAL_REFRESH_COUNT] = current + 1
            }
        }
    }

    /**
     * Record a completed task / successful operation in DataStore
     */
    fun recordTaskCompletion() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[KEY_TASK_COMPLETION_COUNT] ?: 0
                preferences[KEY_TASK_COMPLETION_COUNT] = current + 1
            }
        }
    }

    /**
     * Evaluate the next eligible prompt based on DataStore metrics and governance rules:
     * - Zero Disruption Rule (bypasses when critical flow is active)
     * - Critical updates bypass standard 30-day cooldown
     * - Global 30-day minimum cooldown across all prompts
     * - In-App Rating: 3+ days post-install AND >= 5 manual refreshes or sessions
     * - App Sharing: >= 5 task completions
     */
    suspend fun evaluatePrompt(
        now: Long = System.currentTimeMillis(),
        hasPendingUpdate: Boolean = false,
        isUpdateMandatory: Boolean = false,
        isCriticalFlowActive: Boolean = false
    ): PromptType {
        // 1. Zero Disruption Rule
        if (isCriticalFlowActive) {
            _currentPrompt.value = PromptType.NONE
            return PromptType.NONE
        }

        // 2. Critical updates bypass cooldown
        if (hasPendingUpdate && isUpdateMandatory) {
            _currentPrompt.value = PromptType.CRITICAL_UPDATE
            return PromptType.CRITICAL_UPDATE
        }

        val metrics = getSnapshotMetrics()

        // 3. 30-day minimum global cooldown between ANY prompts
        if (now - metrics.lastPromptTimestamp < COOLDOWN_30_DAYS_MS) {
            _currentPrompt.value = PromptType.NONE
            return PromptType.NONE
        }

        // 4. Flexible Update (highest priority after critical)
        if (hasPendingUpdate) {
            _currentPrompt.value = PromptType.FLEXIBLE_UPDATE
            return PromptType.FLEXIBLE_UPDATE
        }

        // 5. In-App Rating verification
        if (isEligibleForRating(metrics, now)) {
            _currentPrompt.value = PromptType.RATE_APP
            return PromptType.RATE_APP
        }

        // 6. Share Prompt verification
        if (isEligibleForSharing(metrics, now)) {
            _currentPrompt.value = PromptType.SHARE_APP
            return PromptType.SHARE_APP
        }

        _currentPrompt.value = PromptType.NONE
        return PromptType.NONE
    }

    private fun isEligibleForRating(metrics: UserEngagementMetrics, now: Long): Boolean {
        if (metrics.isRated) return false
        if (metrics.ratingPromptCount >= MAX_RATING_PROMPT_ATTEMPTS) return false
        if (now - metrics.lastRatingPromptTime < COOLDOWN_30_DAYS_MS) return false

        val installTime = if (metrics.installTimestamp > 0L) metrics.installTimestamp else now
        val daysInstalled = (now - installTime) / (24 * 60 * 60 * 1000L)

        // Threshold check: >= 3 days post-install and (>= 5 manual refreshes OR >= 5 sessions)
        return daysInstalled >= RATING_MIN_DAYS_INSTALLED &&
                (metrics.manualRefreshCount >= RATING_MIN_MANUAL_REFRESHES || metrics.sessionCount >= RATING_MIN_SESSIONS)
    }

    private fun isEligibleForSharing(metrics: UserEngagementMetrics, now: Long): Boolean {
        if (metrics.isShared) return false
        if (metrics.sharePromptCount >= MAX_SHARE_PROMPT_ATTEMPTS) return false
        if (now - metrics.lastSharePromptTime < COOLDOWN_30_DAYS_MS) return false

        // Threshold check: >= 5 tasks completed
        return metrics.taskCompletionCount >= SHARE_MIN_TASKS
    }

    /**
     * Record that a prompt was shown and persist timestamps to DataStore
     */
    fun recordPromptShown(type: PromptType, timestamp: Long = System.currentTimeMillis()) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_LAST_ANY_PROMPT_TIME] = timestamp
                when (type) {
                    PromptType.RATE_APP -> {
                        preferences[KEY_LAST_RATING_PROMPT_TIME] = timestamp
                        val current = preferences[KEY_RATING_PROMPT_COUNT] ?: 0
                        preferences[KEY_RATING_PROMPT_COUNT] = current + 1
                    }
                    PromptType.SHARE_APP -> {
                        preferences[KEY_LAST_SHARE_PROMPT_TIME] = timestamp
                        val current = preferences[KEY_SHARE_PROMPT_COUNT] ?: 0
                        preferences[KEY_SHARE_PROMPT_COUNT] = current + 1
                    }
                    PromptType.FLEXIBLE_UPDATE, PromptType.CRITICAL_UPDATE -> {
                        preferences[KEY_LAST_UPDATE_PROMPT_TIME] = timestamp
                    }
                    PromptType.NONE -> Unit
                }
            }
            _currentPrompt.value = PromptType.NONE
        }
    }

    fun dismissPrompt() {
        _currentPrompt.value = PromptType.NONE
    }

    fun markRatingCompleted() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_IS_RATED] = true
            }
            _currentPrompt.value = PromptType.NONE
        }
    }

    fun markShareCompleted() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_IS_SHARED] = true
            }
            _currentPrompt.value = PromptType.NONE
        }
    }

    suspend fun getSnapshotMetrics(): UserEngagementMetrics = metricsFlow.first()
}
