package de.mannodermaus.kommandah.utils.extensions

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

private val backgroundScheduler
  get() = Schedulers.computation()
private val mainThreadScheduler
  get() = AndroidSchedulers.mainThread()

fun <T> Flowable<T>.async(): Flowable<T> =
    this.subscribeOn(backgroundScheduler)
        .observeOn(mainThreadScheduler)

fun <T> Observable<T>.async(): Observable<T> =
    this.subscribeOn(backgroundScheduler)
        .observeOn(mainThreadScheduler)

fun <T> Single<T>.async(): Single<T> =
    this.subscribeOn(backgroundScheduler)
        .observeOn(mainThreadScheduler)

fun <T> Maybe<T>.async(): Maybe<T> =
    this.subscribeOn(backgroundScheduler)
        .observeOn(mainThreadScheduler)

fun Completable.async(): Completable =
    this.subscribeOn(backgroundScheduler)
        .observeOn(mainThreadScheduler)
