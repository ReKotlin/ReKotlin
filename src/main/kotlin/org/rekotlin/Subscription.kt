package org.rekotlin

/**
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

/**
 * A box around subscriptions and subscribers.
 *
 * Acts as a type-erasing wrapper around a subscription and its transformed subscription.
 * The transformed subscription has a type argument that matches the selected substate of the
 * subscriber; however that type cannot be exposed to the store.
 *
 * The box subscribes either to the original subscription, or if available to the transformed
 * subscription and passes any values that come through this subscriptions to the subscriber.
 *
 */
class SubscriptionBox<State, SelectedState>(private val originalSubscription: Subscription<State>,
                                            transformedSubscription: Subscription<SelectedState>?,
                                            val subscriber: StoreSubscriber<SelectedState>) where State: StateType {

    // hoping to mimic swift weak reference
    // however this doesn't really work the same way, gc collects non-deterministically
    // setting the original subscriber to null will not result in this being nulled synchronously
    /*
    var _subscriber: WeakReference<StoreSubscriber<SelectedState>> = WeakReference(subscriber)
    val subscriber: StoreSubscriber<SelectedState>?
        get() {
            return _subscriber.get()
        }
    */

    init {
        // If we haven't received a transformed subscription, we forward all values
        // from the original subscription.
        val forwardFromOriginalSubscription = {
            // original Swift implementation has type errased subscriber
            // to avoid casting and passing incompatible value
            // conditional cast was added check
            originalSubscription.observe { _, newState ->
                @Suppress("UNCHECKED_CAST")
                (newState as? SelectedState)?.let {
                    this.subscriber.newState(it)
                }
            }
        }

        // If we received a transformed subscription, we subscribe to that subscription
        // and forward all new values to the subscriber.
        transformedSubscription?.let {
            transformedSubscription.observe { _, newState ->
                this.subscriber.newState(newState)
            }

        } ?: forwardFromOriginalSubscription()
    }

    fun newValues(oldState: State?, newState: State){
        // We pass all new values through the original subscription, which accepts
        // values of type `<State>`. If present, transformed subscriptions will
        // receive this update and transform it before passing it on to the subscriber.
        this.originalSubscription.newValues(oldState, newState)
    }
}

class Subscription<State> {

    @Suppress("FunctionName")
    private fun <Substate> _select(selector: ((State) -> Substate)): Subscription<Substate> {
        return Subscription { sink ->
            this.observe { oldState, newState ->
                sink(oldState?.let(selector), selector(newState))
            }
        }
    }

    // region: Public interface

    /**
     * Provides a subscription that selects a substate of the state of the original subscription.
     * @param selector A closure that maps a state to a selected substate
     */
    fun <Substate> select(selector: ((State) -> Substate)): Subscription<Substate> {
        return this._select(selector)
    }

    /**
     * Provides a subscription that skips certain state updates of the original subscription.
     * @param isRepeat A closure that determines whether a given state update is a repeat and
     * thus should be skipped and not forwarded to subscribers.
     *
     */
    fun skipRepeats(isRepeat: (oldState: State, newState: State) -> Boolean): Subscription<State> {
        return Subscription { sink ->
            this.observe { oldState, newState ->
                oldState?.let {
                    if (!isRepeat(oldState, newState)) {
                        sink(oldState, newState)
                    }

                } ?: sink(oldState, newState)
            }
        }
    }

    /**
     * Provides a subscription that skips repeated updates of the original subscription
     * Repeated updates determined by structural equality
     */
    fun skipRepeats(): Subscription<State> {
        return this.skipRepeats { oldState, newState ->
            oldState == newState
        }
    }

    /** Provides a subscription that skips certain state updates of the original subscription.
     *
     * This is identical to `skipRepeats` and is provided simply for convenience.
     * @param when A closure that determines whether a given state update is a repeat and
     * thus should be skipped and not forwarded to subscribers.
     */
    fun skip(`when`: (oldState: State, newState: State) -> Boolean): Subscription<State> {
        return this.skipRepeats(`when`)
    }

    /**
     * Provides a subscription that only updates for certain state changes.
     *
     * This is effectively the inverse of skipRepeats(:)
     * @param whenBlock A closure that determines whether a given state update should notify
     */
    fun only(whenBlock: (oldState: State, newState: State) -> Boolean): Subscription<State> {
        return this.skipRepeats { oldState, newState ->
            !whenBlock(oldState, newState)
        }
    }

    // endregion

    // region: Internals
    private var observer: ((State?, State) -> Unit)? = null

    init {}
    constructor()

    /// Initializes a subscription with a sink closure. The closure provides a way to send
    /// new values over this subscription.
    private constructor(sink: ((State?, State) -> Unit) -> Unit){
        // Provide the caller with a closure that will forward all values
        // to observers of this subscription.

        sink { old, new ->
            this.newValues(old, new)
        }
    }

    /**
     * Sends new values over this subscription. Observers will be notified of these new values.
     */
    fun newValues(oldState: State?, newState: State){
        this.observer?.invoke(oldState, newState)
    }

    /// A caller can observe new values of this subscription through the provided closure.
    /// - Note: subscriptions only support a single observer.
    internal fun observe(observer: (State?, State) -> Unit){
        this.observer = observer
    }

    // endregion
}