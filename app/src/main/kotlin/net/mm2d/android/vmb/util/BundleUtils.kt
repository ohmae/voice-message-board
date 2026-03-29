/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.util

import android.os.Bundle
import android.os.Parcelable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun buildBundle(
    action: Bundle.() -> Unit,
): Bundle {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return Bundle().apply(action)
}

fun stringBundle(
    key: String,
    value: String,
): Bundle = buildBundle { putString(key, value) }

fun parcelableBundle(
    key: String,
    value: Parcelable,
): Bundle = buildBundle { putParcelable(key, value) }
