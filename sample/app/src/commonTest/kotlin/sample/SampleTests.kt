package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleTests {

    @Test
    fun buttonClickIncrementsCounter() {
        val initialState = GameState.INITIAL_STATE

        gameStore.dispatch(ButtonClickAction())
        assertEquals(gameStore.state.ct, initialState.ct + 1)
    }
}