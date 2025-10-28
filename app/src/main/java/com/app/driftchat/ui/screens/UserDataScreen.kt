package com.app.driftchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import com.app.driftchat.domainmodel.UserData
import com.app.driftchat.ui.components.RandomQuoteGetter
import com.app.driftchat.ui.viewmodels.QuoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDataScreen(
    viewModel: UserDataViewModel,
    quoteViewModel: QuoteViewModel,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit) {
    // Collect the state once at the top
    val userData by viewModel.data.collectAsState()

    if (userData != null &&
        userData!!.quote != null &&
        !userData!!.quote!!.isEmpty() &&
        quoteViewModel.quote.isEmpty()) {

        quoteViewModel.quote = userData!!.quote!!
    }

// Use the collected state to initialize your 'remember' blocks, providing default values
    var name by remember(userData) { mutableStateOf(userData?.name ?: "") }
    var description by remember(userData) { mutableStateOf(userData?.description ?: "") }
    val selectedHobbies = remember(userData) { mutableStateOf(userData?.hobbies ?: emptySet()) }
    val selectedGender = remember(userData) { mutableStateOf(userData?.gender ?: Gender.MALE) } // default to male

    val maxNameLength = 40
    val maxDescriptionLength = 500

    if (name == null) {
        name = ""
    }
    if (description == null) {
        description = ""
    }
    val nameError = name.isEmpty()

    // If name is missing, border red and show error message
    var borderColor = Color.Black
    var errorMessage = ""
    if (nameError) {
        borderColor = Color.Red
        errorMessage = "Required"
    } else {
        borderColor = Color.Black
        errorMessage = ""
    }

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
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        viewModel.updateUserData(
                            UserData(
                                name = name,
                                hobbies = selectedHobbies.value,
                                description = description,
                                gender = selectedGender.value,
                                quote = quoteViewModel.quote,
                            )
                        )
                        if (dragAmount > 50) {
                            onSwipeRight()
                        }
                        if (dragAmount < -50) {
                            onSwipeLeft()
                        }
                    }
                },
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // name field title row
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 50.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "What do you want to be called?",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    )
                }
                // for selecting name
                Row (
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 10.dp),
                ) {
                    TextField(
                        value = name!!,
                        onValueChange = {
                            if (it.length <= maxNameLength) {
                                name = it
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Enter name",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                        ),
                        isError = nameError,
                        // These can be used in the future
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                        ),

                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .border(2.dp, borderColor, RoundedCornerShape(50.dp))
                            .fillMaxWidth()
                    )
                }
                // for displaying error message if missing name
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = Color.Red
                    )
                }

                // for choosing hobbies
                Row (
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 60.dp)
                ) {
                    HobbiesSelector(allHobbies = stringArrayResource(R.array.hobbies_array), selectedHobbies = selectedHobbies)
                }

                // random quote getter
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top= 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Which quote speaks to you?",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    )
                }

                // random quote getter
                Row (
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 60.dp),
                ) {
                    RandomQuoteGetter(quoteViewModel)
                }

                // description box title
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top= 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Write something about yourself",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    )
                }

                // for writing description
                Row (
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 60.dp),
                ) {
                    TextField(
                        value = description!!,
                        onValueChange = {
                            if (it.length <= maxDescriptionLength) {
                                description = it
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        maxLines = 20,
                        textStyle = TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color.Black),
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .defaultMinSize(0.dp, 150.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(30.dp))
                            .shadow(480.dp, RoundedCornerShape(30.dp)),
                    )
                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose your gender:",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    )
                }

                //for choosing gender
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .padding(8.dp),

                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        for (gender: Gender in Gender.entries) {
                            Column(
                                modifier = Modifier
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Text(
                                    text = gender.name,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,)
                                Checkbox(
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color.Black,
                                        uncheckedColor = MaterialTheme.colorScheme.primary,
                                    ),
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