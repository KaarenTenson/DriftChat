package com.app.driftchat.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbiesSelector(
    allHobbies: Array<String>,
    selectedHobbies: MutableState<Set<String>>
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // title
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "What do you like to do?",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = TextStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                textAlign = TextAlign.Center
            ),
            placeholder = {
                Text(
                    text = "Search activities",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(50.dp))
                .fillMaxWidth()
                .shadow(100.dp, RoundedCornerShape(50.dp)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter hobbies by search
        val filteredHobbies = allHobbies.filter {
            it.contains(searchQuery, ignoreCase = true)
                    && !selectedHobbies.value.contains(it)
        }

        // Scrollable list
        LazyColumn(userScrollEnabled = true,
            modifier = Modifier
            .height(200.dp)
            .width(300.dp)
            .border(width = 1.dp,color = Color.Black,RoundedCornerShape(50.dp))
        ) {
                items(filteredHobbies) { hobby ->
                    val checked = hobby in selectedHobbies.value

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                // removes gray background when row is clicked
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                selectedHobbies.value =
                                    if (checked) selectedHobbies.value - hobby
                                    else selectedHobbies.value + hobby
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Button(
                            colors = buttonColors(
                                containerColor = Color.Black
                            ),
                            onClick = {
                                if (selectedHobbies.value.contains(hobby)) {
                                    selectedHobbies.value = selectedHobbies.value - hobby
                                } else {
                                    selectedHobbies.value = selectedHobbies.value + hobby
                                }
                            }
                        ) {
                            Text(
                                text = hobby,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.padding(start = 3.dp, end = 3.dp))
                        }

                    }
                }
            }

        // selected activities
        if (selectedHobbies.value.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = "selected activities:",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
                    .padding(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedHobbies.value.forEach { hobby ->
                        AssistChip(
                            onClick = {
                                // Remove on chip click
                                selectedHobbies.value = selectedHobbies.value - hobby
                            },
                            label = { Text(
                                text = hobby,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ) }
                        )
                    }
                }
            }
        }
    }
}