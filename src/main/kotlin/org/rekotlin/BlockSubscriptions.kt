package org.rekotlin

/**
 * Utility class for holding multiple subscriptions which then can be by store for un-subscription.
 */
class BlockSubscriptions {

    var blockSubscriberList = arrayListOf<BlockSubscriber<*>>()

    fun <T> add(subscriber: BlockSubscriber<T>) {
        blockSubscriberList.add(subscriber)
    }
}