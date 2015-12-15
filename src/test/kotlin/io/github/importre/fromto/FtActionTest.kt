package io.github.importre.fromto

import org.junit.Test
import rx.Observable
import kotlin.test.assertFalse

class FtActionTest {

    @Test
    fun ShouldBeInitialized() {
        assertFalse {
            val action = FtAction.Builder<Int>()
                    .from(Observable.just(1))
                    .to {}
                    .error {}
                    .done {}
                    .build()
            action.isRunning()
        }

        assertFalse {
            val action = FtAction.Builder<Int>()
                    .from(Observable.just(1))
                    .build()
            action.isRunning()
        }
    }

    @Test(expected = NullPointerException::class)
    fun ShouldNotBeInitializedWithoutFrom() {
        FtAction.Builder<Int>()
                .to {}
                .error {}
                .build()
    }
}