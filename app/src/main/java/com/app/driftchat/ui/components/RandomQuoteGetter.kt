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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.driftchat.ui.viewmodels.QuoteViewModel

// Component for getting a random quote from the API and displaying it in a box
@Composable
fun RandomQuoteGetter(QuoteViewModel: QuoteViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .border(width = 2.dp, color = MaterialTheme.colorScheme.secondary, RoundedCornerShape(25.dp))
                .clip(RoundedCornerShape(25.dp))
                .fillMaxSize(),
        ) { // if no other API calls in progress, show the random quote from the viewmodel
            if (!QuoteViewModel.isLoading) {
                Text(
                    text = QuoteViewModel.quote,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(25.dp)
                ) // if another API call is in progress show loading circle
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
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            onClick = { QuoteViewModel.fetchRandomQuote() }, // API call
            enabled = !QuoteViewModel.isLoading, // disables button if another API call in progress
        ) {
            Text(
                text = "Get Random Quote",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}