package com.vocab.feature.review

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ReviewRoute

/** [onSessionActive] reports whether cards are being reviewed, so the shell can hide its dock meanwhile. */
fun NavGraphBuilder.reviewScreen(onClose: () -> Unit, onSessionActive: (Boolean) -> Unit) {
    composable<ReviewRoute> { ReviewScreen(onClose, onSessionActive) }
}
