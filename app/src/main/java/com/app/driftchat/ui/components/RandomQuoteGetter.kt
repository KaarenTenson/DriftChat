package com.app.driftchat.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.driftchat.ui.viewmodels.QuoteViewModel

@Composable
fun RandomQuoteGetter(QuoteViewModel: QuoteViewModel = viewModel()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .border(width = 2.dp, color = Color.Black, RoundedCornerShape(25.dp))
                .clip(RoundedCornerShape(25.dp))
                .fillMaxSize(),
        ) {
            if (!QuoteViewModel.isLoading) {
                Text(
                    text = QuoteViewModel.quote,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(25.dp)
                )
            } else if (QuoteViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                        .padding(30.dp),
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            colors = buttonColors(
                containerColor = Color.Black
            ),
            onClick = { QuoteViewModel.fetchRandomQuote() },
            enabled = !QuoteViewModel.isLoading,
        ) {
            Text(
                text = "Get Random Quote",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}