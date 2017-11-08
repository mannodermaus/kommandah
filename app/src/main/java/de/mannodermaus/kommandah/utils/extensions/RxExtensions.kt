package de.mannodermaus.kommandah.utils.extensions

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Flowable<T>.async(): Flowable<T> =
    this.subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
