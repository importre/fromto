package io.github.importre.fromto

import rx.Observable
import rx.Subscription
import rx.subjects.Subject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [FtAction] executes [rx.Observable] and notifies [rx.Subject] of the result.
 *
 * @author [Jaewe Heo](http://import.re)
 *
 * @param T represents the action&apos;s result data
 */
public class FtAction<T> private constructor() {

    private lateinit var fromObservable: Observable<T>

    private lateinit var toSubject: Subject<T, T>
    private lateinit var dataView: ((T) -> Unit)

    private var finishView: (() -> Unit)? = null
    private var finishSubject: Subject<Unit, Unit>? = null
    private var errorSubject: Subject<Throwable, Throwable>? = null
    private var errorView: ((Throwable) -> Unit)? = null

    private var fromSubscription: Subscription? = null
    private var toSubscription: Subscription? = null
    private var finishSubscription: Subscription? = null
    private var errorSubscription: Subscription? = null

    private var running: AtomicBoolean = AtomicBoolean(false)
        private set

    /**
     * @return true if it is running, otherwise false
     */
    public fun isRunning() = running.get()

    private fun init() {
        toSubscription?.unsubscribe()
        toSubscription = toSubject.subscribe({
            dataView.invoke(it)
        }, {
            errorSubject?.onNext(it)
        })

        finishSubscription?.unsubscribe()
        finishSubscription = finishSubject?.subscribe({
            finishView?.invoke()
        }, {
        })

        errorSubscription?.unsubscribe()
        errorSubscription = errorSubject?.subscribe({
            errorView?.invoke(it)
        }, {
            errorView?.invoke(it)
        })
    }

    fun subscribe(fromTo: FromTo) {
        fromSubscription?.unsubscribe()
        fromTo.run { if (!isLoading()) view?.showLoading(true) }
        running.set(true)

        fromSubscription = fromObservable.subscribe({
            toSubject.onNext(it)
        }, {
            errorSubject?.onError(it)
            finished(fromTo)
        }, {
            finishSubject?.onNext(Unit)
            finished(fromTo)
        })
    }

    private fun finished(fromTo: FromTo) {
        running.set(false)
        fromTo.run { if (!isLoading()) view?.showLoading(false) }
    }

    fun unsubscribe(fromTo: FromTo) {
        fromSubscription?.unsubscribe()
        toSubscription?.unsubscribe()
        finished(fromTo)
    }

    /**
     *  [FtAction]&apos;s builder.
     */
    public class Builder<T>() {

        private var dataView: ((T) -> Unit)? = null
        private var finishView: (() -> Unit)? = null
        private var errorView: ((Throwable) -> Unit)? = null
        private var fromObservable: Observable<T>? = null
        private var toSubject: Subject<T, T>? = null
        private var finishSubject: Subject<Unit, Unit>? = null
        private var errorSubject: Subject<Throwable, Throwable>? = null

        /**
         * Sets observable that will be subscribed. It is must be called.
         *
         * @param fromObservable
         * @return [FtAction.Builder]
         */
        public fun from(fromObservable: Observable<T>): Builder<T> {
            this.fromObservable = fromObservable
            return this
        }

        /**
         * Sets subject and view of `to`. It is must be called.
         * [dataView] will be invoked with the result via [toSubject].`onNext()`
         *
         * @param toSubject
         * @param dataView
         * @return [FtAction.Builder]
         */
        public fun to(toSubject: Subject<T, T>,
                      dataView: ((T) -> Unit)): Builder<T> {
            this.toSubject = toSubject
            this.dataView = dataView
            return this
        }

        /**
         * Sets subject and view of `finish`.
         * [finishView] will be invoked with the result via [finishSubject].`onNext()`
         *
         * @param finishSubject
         * @param finishView
         * @return [FtAction.Builder]
         */
        public fun finish(finishSubject: Subject<Unit, Unit>,
                          finishView: (() -> Unit)): Builder<T> {
            this.finishSubject = finishSubject
            this.finishView = finishView
            return this
        }

        /**
         * Sets subject and view of `error`.
         * [errorView] will be invoked with the result via [errorSubject].`onNext()`
         *
         * @param errorSubject
         * @param errorView
         * @return [FtAction.Builder]
         */
        public fun error(errorSubject: Subject<Throwable, Throwable>,
                         errorView: ((Throwable) -> Unit)): Builder<T> {
            this.errorSubject = errorSubject
            this.errorView = errorView
            return this
        }

        /**
         * Constructs a [FtAction] with the current attributes.
         *
         * @return [FtAction]
         */
        public fun build(): FtAction<T> {
            val action = FtAction<T>()

            if (fromObservable == null) {
                throw NullPointerException("[FROMTO] `from` observable is null")
            } else {
                action.fromObservable = fromObservable as Observable<T>
            }

            if (dataView == null) {
                throw NullPointerException("[FROMTO] `dataView` is null")
            } else {
                action.dataView = dataView as (T) -> Unit
            }

            if (toSubject == null) {
                throw NullPointerException("[FROMTO] `to` subject is null")
            } else {
                action.toSubject = toSubject as Subject<T, T>
            }

            action.finishView = finishView
            action.finishSubject = finishSubject as? Subject<Unit, Unit>
            action.errorView = errorView
            action.errorSubject = errorSubject as? Subject<Throwable, Throwable>
            action.init()
            return action
        }
    }
}