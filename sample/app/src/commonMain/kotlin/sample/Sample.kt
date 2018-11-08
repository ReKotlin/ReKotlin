package sample

import org.rekotlin.*

/**
 * Simple example of a Multiplatform (Android & iOS) app using ReKotlin.
 * In a more production app these would be put into appropriate files/packages.
 */

data class GameState(val ct: Int) : StateType {
    companion object {
        val INITIAL_STATE = GameState(1)
    }
}

class ButtonClickAction: Action

fun reducer(action: Action, state: GameState?): GameState {
    return when (action) {
        is ButtonClickAction -> state!!.copy(ct = state.ct + 1)
        else -> throw IllegalArgumentException("Unhandled Action")
    }
}

val gameStore = Store(
    reducer = ::reducer,
    state = GameState.INITIAL_STATE,
    middleware = listOf()
)