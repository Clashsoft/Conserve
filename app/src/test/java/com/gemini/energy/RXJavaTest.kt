package com.gemini.energy

import io.reactivex.*
import org.junit.Assert.assertEquals
import org.junit.Test

class RXJavaTest {
    @Test
    fun returnAValue() {
        var result = ""
        val observer: Observable<String> = Observable.just("Hello")
        observer.subscribe { s -> result = s }
        assertEquals("Hello", result)
    }


    @Test
    fun flowableTest() {
        val flowable = Flowable.create<Int>({ emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)

        flowable.subscribe {
            println(it)
        }

    }

}
