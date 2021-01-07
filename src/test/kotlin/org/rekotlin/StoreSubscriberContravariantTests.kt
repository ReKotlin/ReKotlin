package org.rekotlin

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class StoreSubscriberContravariantTests {

    @Mock
    lateinit var subscriber: StoreSubscriber<BaseAppState>

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun subscriberCanBeCovariantTypeToStoreStateType() {
        // Arrange
        val appState = AppState()
        val store = Store(AppStateReducer(), appState, emptyList(), false)
        // Act
        // A StoreSubscriber of a super type should be an acceptable type to be passed into a Store.
        store.subscribe(subscriber)
        store.dispatch(AppAction())

        // Assert
        verify(subscriber, times(2)).newState(safeEq(appState))
    }

    interface BaseAppState : StateType
    internal class AppAction : Action
    internal data class AppState(val userName: String = "") : BaseAppState
    internal class AppStateReducer : Reducer<AppState> {
        override fun invoke(action: Action, state: AppState?): AppState = state ?: AppState()
    }

    private fun <T : Any> safeEq(value: T): T = eq(value) ?: value
}
