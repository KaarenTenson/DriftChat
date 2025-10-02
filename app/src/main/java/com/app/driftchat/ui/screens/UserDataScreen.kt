package com.app.driftchat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.driftchat.R
import com.app.driftchat.domainmodel.Gender
import com.app.driftchat.ui.components.HobbiesSelector
import com.app.driftchat.ui.viewmodels.UserDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDataScreen(viewModel: UserDataViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedHobbies = remember { mutableStateOf(setOf<String>()) }
    val selectedGender = remember { mutableStateOf(Gender.MALE) }

    var nameError = name.isEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.swipe_left))
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.swipe_right),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                //for selecting name
                Row (
                    modifier = Modifier.padding(8.dp),
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.enter_name)) },
                        maxLines = 2,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
                        isError = nameError,
                        trailingIcon = {
                            if (nameError) Icon(Icons.Default.Warning, contentDescription = null)
                        },
                        supportingText = {
                            if (nameError) Text(stringResource(R.string.name_error))
                        },

                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    )

                }

                //for choosing hobbies
                Row (
                    modifier = Modifier.padding(8.dp),
                ) {
                    HobbiesSelector(allHobbies = stringArrayResource(R.array.hobbies_array), selectedHobbies = selectedHobbies)
                }
                //for writing description
                Row (
                    modifier = Modifier.padding(8.dp),
                ) {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.enter_desc)) },
                        maxLines = 30,
                        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(20.dp)
                            .defaultMinSize(0.dp, 150.dp)
                            .fillMaxWidth()
                    )
                }

                //for choosing gender
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.choose_gender))

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                    ) {
                        for (gender: Gender in Gender.entries) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(gender.name)
                                Checkbox(
                                    checked = selectedGender.value == gender,
                                    onCheckedChange = { selectedGender.value = gender }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
