package io.github.importre.fromto

import org.junit.Test
import rx.Observable
import rx.subjects.PublishSubject
import kotlin.test.assertFalse

class FtActionTest {

    @Test
    fun ShouldBeInitialized() {
        assertFalse {
            val action = FtAction.Builder<Int>()
                    .from(Observable.just(1))
                    .to({}, PublishSubject.create<FtResult<Int>>())
                    .error({}, PublishSubject.create<Throwable>())
                    .build()
            action.isRunning()
        }

        assertFalse {
            val action = FtAction.Builder<Int>()
                    .from(Observable.just(1))
                    .to({}, PublishSubject.create<FtResult<Int>>())
                    .build()
            action.isRunning()
        }
    }

    @Test(expected = NullPointerException::class)
    fun ShouldNotBeInitializedWithoutFrom() {
        FtAction.Builder<Int>()
                .to({}, PublishSubject.create<FtResult<Int>>())
                .error({}, PublishSubject.create<Throwable>())
                .build()
    }

    @Test(expected = NullPointerException::class)
    fun ShouldNotBeInitializedWithoutTo() {
        FtAction.Builder<Int>()
                .from(Observable.just(1))
                .error({}, PublishSubject.create<Throwable>())
                .build()
    }
}