# FromTo

[![Download][bintray-badge]][bintray-url]


**FromTo** is a helper library that makes it simple to handle asynchronous actions with views for Android and Java/Kotlin.

This library is written in [Kotlin][kotlin]. But it's interoperable with Java.


## Installation

Set to your `build.gradle`.

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'io.github.importre:fromto:<version>'
}
```


## Getting Started

```kotlin
// make your job
val job = Observable.just(1)
        .subscribeOn(Schedulers.newThread())

// define an action and handlers you want
val action1 = FtAction.Builder<Int>()
        .from(job)
        .to { println(it) }
        .build()

val fromTo = FromTo.create(action1/*, more if you want */)

// attach `view`(FtView) related with fromTo's lifecycle
fromTo.attach(view).execute()

// you can know `loading` state via `view`

// detach when `view` is destroyed
fromTo.detach()
```

Basically `FromTo` has an `FtAction` or more.  
(You can think that `FtAction` is a wrapper class of `rx.Observable`.)  
Given some actions, `FromTo` can execute all actions asynchronously. And `FromTo` notifies corresponding view of the `loading` state via `FtView` interface.

> - Synchronous or asynchronous action
>   - Actually it's up to observable's schedule as you know.
> - The state is `true` if one of actions is working.

See details in Kotlin example

### [Example][example]

![fromto-example](https://goo.gl/svxxDD)

See examples.

- [Kotlin example][kotlin-example]
  - Check comments :sunglasses:
- [Java example][java-example]

## Test

```sh
./gradlew test
```


## Why should I use it?

**FromTo** is useful when developing an app, which has lifecycle by itself.  
In Android world, for example, An `Activity`(or a `Fragment`) can take some heavy tasks such as Network, DB, File I/O and/or something.

Assume that you are in two conditions.

- two tasks are working asynchronously,
- you have to show `ProgressBar` if started, hide it if finished.

In these cases, how do you resolve this problem if your screen is rotated?  
I know and you know some solutions.  

- Do nothing. :trollface:
  - Easy but bad UX. Everything will be always reloaded.
- Prevent recreating activity.
  - Using `android:configChanges` of &lt;[activity][activity-element]&gt; element. It's very easy but the layout is not flexible(on tablet especially).
- Use `AsyncTask`, `Handler` or whatever. And [Store/Restore data][recreating-activity]
  - Inconvenient, verbose :sob:
- Set `Fragment.setRetainInstance(true)`
  - The fragment is not recreated/destroyed. So the fragment can store what you want although parent activity is recreated.
  - It's good. But some guys wouldn't like to use Fragment.
- **Use [RxJava]**
  - `Observable`'s `cache()` is a blessing. :tada:
  - See `Lifecycle` section of [this post][cache].

I thought that `RxJava` is the best solution because it's very simple and convenient.  
But I realized that there are still boilerplate codes. So I implemented **FromTo** simply.


## License

Apache 2.0 © [Jaewe Heo](http://import.re)




[activity-element]: http://developer.android.com/intl/ko/guide/topics/manifest/activity-element.html
[recreating-activity]: http://developer.android.com/training/basics/activity-lifecycle/recreating.html
[RxJava]: https://github.com/ReactiveX/RxJava
[cache]: http://blog.danlew.net/2014/10/08/grokking-rxjava-part-4/
[example]: https://github.com/importre/fromto-example
[bintray-badge]: https://api.bintray.com/packages/importre/maven/fromto/images/download.svg
[bintray-url]: https://bintray.com/importre/maven/fromto/_latestVersion
[kotlin]: http://kotlinlang.org/
[kotlin-example]: https://goo.gl/YQJ8YK
[java-example]: https://goo.gl/RRF54v
