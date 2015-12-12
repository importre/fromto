package io.github.importre.fromto

import org.junit.Test
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FromToTest {

    val intSubject = PublishSubject.create<Int>()
    val errorSubject = PublishSubject.create<Throwable>()
    val view = object : FtView {
        override fun showLoading(show: Boolean) {
        }
    }

    @Test
    fun ShouldBeInitialized() {
        val action = FtAction.Builder<Int>()
                .from(Observable.just(1))
                .to(intSubject, {})
                .error(errorSubject, {})
                .build()
        val fromto = FromTo.create(action)
        assertEquals(1, fromto.actions.size)
    }

    @Test
    fun ShouldBeSuccessful() {
        val size = 2
        val count = CountDownLatch(size)

        val action1 = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to(intSubject, { count.countDown() })
                .error(errorSubject, {})
                .build()

        val action2 = FtAction.Builder<Int>()
                .from(Observable.just(1).subscribeOn(Schedulers.newThread()))
                .to(intSubject, { count.countDown() })
                .error(errorSubject, {})
                .build()

        val fromto = FromTo.create(action1, action2)
        fromto.attach(view).execute()
        assertTrue(fromto.isLoading())

        count.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, count.count)

        fromto.detach()
        assertFalse(fromto.isLoading())
    }

    @Test
    fun ShouldBeFailed() {
        val count = CountDownLatch(1)

        val action = FtAction.Builder<Int>()
                .from(Observable.create<Int> {
                    val a: Int? = null
                    println(a.toString())
                    a!!
                }.subscribeOn(Schedulers.newThread()))
                .to(intSubject, { })
                .error(errorSubject, { count.countDown() })
                .build()

        val fromto = FromTo.create(action)
        fromto.attach(view).execute()
        assertTrue(fromto.isLoading())

        count.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, count.count)

        fromto.detach()
        assertFalse(fromto.isLoading())
    }
}