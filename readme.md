# FromTo

[![Download][bintray-badge]][bintray-url]


**FromTo** is a helper library that makes it simple to handle asynchronous actions for Android and Java/Kotlin.

This library is written in [Kotlin][kotlin]. But it's interoperable with Java.


## Installation

Set to your `build.gradle`.

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'io.github.importre:fromto:0.5.0'
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

// detach when `view` is destroyed
fromTo.detach()
```


## Why should I use it?

**FromTo** is useful when developing an app, which has lifecycle by itself.  
In Android world, for example, An Activity(or a Fragment) can take some heavy tasks such as Network, DB, File I/O and/or something.

Assume that you are in two conditions.

- two tasks are working asynchronously,
- you have to show `ProgressBar` if started, hide it if finished.

In this case, how do you resolve this problem if your screen is rotated?  
I know and you know some solutions.  

1. Do nothing. :trollface:
  - Easy but bad UX. Everything will be always reloaded.
1. Prevent recreating activity.
  - Using `android:configChanges` of &lt;[activity][activity-element]&gt; element. It's very easy but the layout is not flexible(on tablet especially).
1. Use `AsyncTask`, `Handler` or whatever. And [Store/Restore data][recreating-activity]
  - Inconvenient, verbose :sob:
1. Set `Fragment.setRetainInstance(true)`
  - The fragment is not recreated/destroyed. So the fragment can store you data although parent activity is rotated.
  - It's good. But some guys wouldn't like to use Fragment.
1. Use [RxJava]
  - `Observable`'s `cache()` is a blessing. :tada:
  - See `Lifecycle` section of [this post][cache].

I think that `RxJava` is the best solution because it's very simple and convenient.  
But I realized that there are still boilerplate codes. So I implemented **FromTo**.


## How do I use it in detail?

Basically `FromTo` has an `FtAction` or more.  
Given some actions, `FromTo` can execute all actions asynchronously. And `FromTo` notifies corresponding view of the `loading` state via `FtView` interface.

> - Synchronous or asynchronous action
>   - Actually it's up to observable's schedule.
> - The state is `true` if one of actions is working.

See [this example][example].







## Test

```sh
./gradlew test
```


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
