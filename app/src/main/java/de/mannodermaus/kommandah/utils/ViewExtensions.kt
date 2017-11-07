package de.mannodermaus.kommandah.utils

import android.support.annotation.StyleRes
import android.support.v7.widget.Toolbar

fun Toolbar.setTitleTextAppearance(@StyleRes res: Int) =
    this.setTitleTextAppearance(context, res)
