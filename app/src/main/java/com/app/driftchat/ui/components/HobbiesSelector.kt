package com.app.driftchat.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.app.driftchat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbiesSelector(
    allHobbies: Array<String>,
    selectedHobbies: MutableState<Set<String>>
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.choose_hobbies), style = MaterialTheme.typography.titleMedium)

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.search_hobbies)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter hobbies by search
        val filteredHobbies = allHobbies.filter {
            it.contains(searchQuery, ignoreCase = true)
                    && !selectedHobbies.value.contains(it)
        }

        // Scrollable list
        LazyColumn(userScrollEnabled = true,
            modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)) {
                items(filteredHobbies) { hobby ->
                    val checked = hobby in selectedHobbies.value

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedHobbies.value =
                                    if (checked) selectedHobbies.value - hobby
                                    else selectedHobbies.value + hobby
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Button(onClick = {
                                if (selectedHobbies.value.contains(hobby)) {
                                    selectedHobbies.value = selectedHobbies.value - hobby
                                } else {
                                    selectedHobbies.value = selectedHobbies.value + hobby
                                }
                        },

                        ) {
                            Text(hobby, modifier = Modifier.padding(start = 8.dp))
                        }

                    }
                }
            }


        if (selectedHobbies.value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
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
                            label = { Text(hobby) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}