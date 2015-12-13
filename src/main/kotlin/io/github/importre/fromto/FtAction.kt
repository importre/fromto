package io.github.importre.fromto

import rx.Observable
import rx.Subscription
import rx.subjects.Subject
import java.util.concurrent.atomic.AtomicBoolean

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

    class Builder<T>() {

        private var dataView: ((T) -> Unit)? = null
        private var finishView: (() -> Unit)? = null
        private var errorView: ((Throwable) -> Unit)? = null
        private var fromObservable: Observable<T>? = null
        private var toSubject: Subject<T, T>? = null
        private var finishSubject: Subject<Unit, Unit>? = null
        private var errorSubject: Subject<Throwable, Throwable>? = null

        fun from(fromObservable: Observable<T>): Builder<T> {
            this.fromObservable = fromObservable
            return this
        }

        fun to(toSubject: Subject<T, T>,
               dataView: ((T) -> Unit)): Builder<T> {
            this.toSubject = toSubject
            this.dataView = dataView
            return this
        }

        fun finish(finishSubject: Subject<Unit, Unit>,
                   finishView: (() -> Unit)): Builder<T> {
            this.finishSubject = finishSubject
            this.finishView = finishView
            return this
        }

        fun error(errorSubject: Subject<Throwable, Throwable>,
                  errorView: ((Throwable) -> Unit)): Builder<T> {
            this.errorSubject = errorSubject
            this.errorView = errorView
            return this
        }

        fun build(): FtAction<T> {
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