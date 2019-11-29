package com.altimedia.updater.domain.state

import android.util.SparseArray
import java.util.concurrent.atomic.AtomicInteger

/**
 * Controls updater state.
 */
class UpdaterState(state: Int) {
    private val mState: AtomicInteger = AtomicInteger(state)

    /**
     * Returns updater state.
     */
    fun get(): Int {
        return mState.get()
    }

    /**
     * Sets the updater state.
     *
     * @throws InvalidTransitionException if transition is not allowed.
     */
    @Throws(InvalidTransitionException::class)
    fun set(newState: Int) {
        val oldState = mState.get()
        TRANSITIONS[oldState]
            .also { requireNotNull(it) }
            ?.let { set ->
            if(!set.contains(newState)) {
                throw InvalidTransitionException(
                    "Can't transition from $oldState to $newState"
                )
            }
        }

        mState.set(newState)
    }

    /**
     * Defines invalid state transition exception.
     */
    class InvalidTransitionException(msg: String) : Exception(msg)

    companion object {
        const val IDLE = 0
        const val ERROR = 1
        const val RUNNING = 2
        const val PAUSED = 3
        const val SLOT_SWITCH_REQUIRED = 4
        const val REBOOT_REQUIRED = 5
        private val STATE_MAP: SparseArray<String> = SparseArray()

        /**
         * Allowed state transitions. It's a map: key is a state, value is a set of states that
         * are allowed to transition to from key.
         */
        private val TRANSITIONS: Map<Int, Set<Int>> =
            mapOf(
                IDLE to setOf(
                    IDLE,
                    ERROR,
                    RUNNING
                ),
                ERROR to setOf(
                    IDLE
                ),
                RUNNING to setOf(
                    IDLE,
                    ERROR,
                    PAUSED,
                    REBOOT_REQUIRED,
                    SLOT_SWITCH_REQUIRED
                ),
                PAUSED to setOf(
                    ERROR,
                    RUNNING,
                    IDLE
                ),
                SLOT_SWITCH_REQUIRED to setOf(
                    ERROR,
                    REBOOT_REQUIRED,
                    IDLE
                ),
                REBOOT_REQUIRED to setOf(
                    IDLE
                )
            )

        /**
         * Converts status code to status name.
         */
        fun getStateText(state: Int) = STATE_MAP[state]

        init {
            STATE_MAP.put(0, "IDLE")
            STATE_MAP.put(1, "ERROR")
            STATE_MAP.put(2, "RUNNING")
            STATE_MAP.put(3, "PAUSED")
            STATE_MAP.put(4, "SLOT_SWITCH_REQUIRED")
            STATE_MAP.put(5, "REBOOT_REQUIRED")
        }
    }
}