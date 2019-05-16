package org.rekotlin

/**
 * BlockSubscriber allows subscribing to multiple states in a single class.
 *
 * Ex
 * val subscriber : BlockSubscriber<MyState>= BlockSubscriber { myState -> }
 * val subscriber2 : BlockSubscriber<MyState2>= BlockSubscriber { myState2 -> }
 *
 * store.subscribe(subscriber)
 * store.subscribe(subscriber2)
 */
class BlockSubscriber<S>(private val block: (S) -> Unit) : StoreSubscriber<S> {

    override fun newState(state: S) {
        block.invoke(state)
    }
}