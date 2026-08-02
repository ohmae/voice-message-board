package net.mm2d.android.vmb.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

fun <T> Flow<T>.stateWhileSubscribedIn(
    scope: CoroutineScope,
    initialValue: T,
): StateFlow<T> = stateIn(scope, SharingStarted.WhileSubscribed(5_000), initialValue)
