package org.rekotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Created by Taras Vozniuk on 10/08/2017.
 * Copyright © 2017 GeoThings. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

internal class StoreSubscriptionTests {

    var reducer = TestReducer()
    var store = Store(reducer::handleAction, TestAppState())

    // this is not going to work in JVM.
    // WeakReference also can't solve it since gc collects non-deterministically
    //TODO: Discuss with ReSwift community for this inconsistency
    /*
    /**
     * It does not strongly capture an observer
     */
    @Test
    fun testStrongCapture(){
        store = Store(reducer::handleAction, TestAppState())
        var subscriber: TestSubscriber? = TestSubscriber()

        store.subscribe(subscriber!!)
        assertEquals(1, store.subscriptions.map { it.subscriber != null }.count())

        @Suppress("UNUSED_VALUE")
        subscriber = null
        assertEquals(0, store.subscriptions.map { it.subscriber != null }.count())
    }
    */

    /**
     * it removes subscribers before notifying state changes
     */
    @Test
    fun testRemoveSubscribers() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber1 = TestSubscriber()
        val subscriber2 = TestSubscriber()

        store.subscribe(subscriber1)
        store.subscribe(subscriber2)
        store.dispatch(SetValueAction(3))
        assertEquals(2, store.subscriptions.count())
        assertEquals(3, subscriber1.recievedStates.lastOrNull()?.testValue)
        assertEquals(3, subscriber2.recievedStates.lastOrNull()?.testValue)

        // dereferencing won't remove the subscriber(like in ReSwift)
        // subscriber1 = null
        store.unsubscribe(subscriber1)
        store.dispatch(SetValueAction(5))
        assertEquals(1, store.subscriptions.count())
        assertEquals(5, subscriber2.recievedStates.lastOrNull()?.testValue)

        // dereferencing won't remove the subscriber(like in ReSwift)
        // subscriber1 = null
        store.unsubscribe(subscriber2)
        store.dispatch(SetValueAction(8))
        assertEquals(0, store.subscriptions.count())
    }

    /**
     * it replaces the subscription of an existing subscriber with the new one.
     */
    @Test
    fun testDuplicateSubscription() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber = TestSubscriber()

        // initial subscription
        store.subscribe(subscriber)
        // Subsequent subscription that skips repeated updates.
        store.subscribe(subscriber) { it.skipRepeats { oldState, newState -> oldState.testValue == newState.testValue } }

        // One initial state update for every subscription.
        assertEquals(2, subscriber.recievedStates.count())

        store.dispatch(SetValueAction(3))
        store.dispatch(SetValueAction(3))
        store.dispatch(SetValueAction(3))
        store.dispatch(SetValueAction(3))

        assertEquals(3, subscriber.recievedStates.count())
    }

    /**
     * it dispatches initial value upon subscription
     */
    @Test
    fun testDispatchInitialValue() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber = TestSubscriber()

        store.subscribe(subscriber)
        store.dispatch(SetValueAction(3))

        assertEquals(3, subscriber.recievedStates.lastOrNull()?.testValue)
    }

    /**
     * it allows dispatching from within an observer
     */
    @Test
    fun testAllowDispatchWithinObserver() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber = DispatchingSubscriber(store)

        store.subscribe(subscriber)
        store.dispatch(SetValueAction(2))

        assertEquals(5, store.state.testValue)
    }

    /**
     * it does not dispatch value after subscriber unsubscribes
     */
    @Test
    fun testDontDispatchToUnsubscribers() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber = TestSubscriber()

        store.dispatch(SetValueAction(5))
        store.subscribe(subscriber)
        store.dispatch(SetValueAction(10))

        store.unsubscribe(subscriber)
        // Following value is missed due to not being subscribed:
        store.dispatch(SetValueAction(15))
        store.dispatch(SetValueAction(25))

        store.subscribe(subscriber)
        store.dispatch(SetValueAction(20))

        assertEquals(4, subscriber.recievedStates.count())
        assertEquals(5, subscriber.recievedStates[subscriber.recievedStates.count() - 4].testValue)
        assertEquals(10, subscriber.recievedStates[subscriber.recievedStates.count() - 3].testValue)
        assertEquals(25, subscriber.recievedStates[subscriber.recievedStates.count() - 2].testValue)
        assertEquals(20, subscriber.recievedStates[subscriber.recievedStates.count() - 1].testValue)
    }

    /**
     * it ignores identical subscribers
     */
    @Test
    fun testIgnoreIdenticalSubscribers() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber = TestSubscriber()

        store.subscribe(subscriber)
        store.subscribe(subscriber)

        assertEquals(1, store.subscriptions.count())
    }

    /**
     * it ignores identical subscribers that provide substate selectors
     */
    @Test
    fun testIgnoreIdenticalSubstateSubscribers() {
        store = Store(reducer::handleAction, TestAppState())
        val subscriber = TestSubscriber()

        store.subscribe(subscriber) { it }
        store.subscribe(subscriber) { it }

        assertEquals(1, store.subscriptions.count())
    }

    @Test
    fun testBlockSubscriber() {
        val store = Store(reducer = ::blockStateReducer, state = null)

        val subscriber = BlockSubscriber<TestAppState?> { }
        val subscriber2 = BlockSubscriber<TestStringAppState?> { }
        val blockSubscriptions = BlockSubscriptions()

        store.subscribe(subscriber) { it.select { it.testAppState } }
        store.subscribe(subscriber2) { it.select { it.testStringAppState } }

        blockSubscriptions.add(subscriber)
        blockSubscriptions.add(subscriber2)

        assertEquals(2, store.subscriptions.count())

        store.unsubscribe(blockSubscriptions)
        assertEquals(0, store.subscriptions.count())
    }
}