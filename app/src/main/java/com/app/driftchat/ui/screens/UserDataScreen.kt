package com.app.driftchat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.driftchat.R
import com.app.driftchat.domainmodel.Gender
import com.app.driftchat.domainmodel.UserData
import com.app.driftchat.ui.components.HobbiesSelector
import com.app.driftchat.ui.components.RandomQuoteGetter
import com.app.driftchat.ui.viewmodels.QuoteViewModel
import com.app.driftchat.ui.viewmodels.UserDataViewModel
import kotlinx.coroutines.delay

private var swipeHintShownThisLaunch = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDataScreen(
    viewModel: UserDataViewModel,
    quoteViewModel: QuoteViewModel,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val userData by viewModel.data.collectAsState()

    if (userData?.quote?.isNotEmpty() == true && quoteViewModel.quote.isEmpty()) {
        quoteViewModel.quote = userData!!.quote!!
    }

    var name by remember(userData) { mutableStateOf(userData?.name ?: "") }
    var description by remember(userData) { mutableStateOf(userData?.description ?: "") }
    val selectedHobbies = remember(userData) { mutableStateOf(userData?.hobbies ?: emptySet()) }
    val selectedGender = remember(userData) { mutableStateOf(userData?.gender ?: Gender.MALE) }

    val nameError = name.isEmpty()
    val borderColor = if (nameError) Color.Red else MaterialTheme.colorScheme.secondary

    var showSwipeOverlay by remember {
        mutableStateOf(!swipeHintShownThisLaunch)
    }

    LaunchedEffect(Unit) {
        if (showSwipeOverlay) {
            swipeHintShownThisLaunch = true
            delay(5000)
            showSwipeOverlay = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            showSwipeOverlay = false

                            viewModel.updateUserData(
                                UserData(
                                    name = name,
                                    hobbies = selectedHobbies.value,
                                    description = description,
                                    gender = selectedGender.value,
                                    quote = quoteViewModel.quote,
                                )
                            )

                            if (dragAmount > 50) onSwipeRight()
                            if (dragAmount < -50) onSwipeLeft()
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {

                    Text(
                        text = "What do you want to be called?",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    TextField(
                        value = name,
                        onValueChange = { if (it.length <= 40) name = it },
                        isError = nameError,
                        modifier = Modifier
                            .padding(20.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .border(2.dp, borderColor, RoundedCornerShape(50.dp))
                            .fillMaxWidth(),
                        textStyle = TextStyle(textAlign = TextAlign.Center),
                        placeholder = {
                            Text(
                                "Enter name",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    HobbiesSelector(
                        allHobbies = stringArrayResource(R.array.hobbies_array),
                        selectedHobbies = selectedHobbies
                    )

                    Text(
                        text = "Which quote speaks to you?",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    RandomQuoteGetter(quoteViewModel)

                    Text(
                        text = "Write something about yourself",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    TextField(
                        value = description,
                        onValueChange = { if (it.length <= 500) description = it },
                        modifier = Modifier
                            .padding(20.dp)
                            .defaultMinSize(minHeight = 150.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp))
                            .border(2.dp, MaterialTheme.colorScheme.secondary),
                        maxLines = 20
                    )

                    Text(
                        text = "Choose your gender:",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (gender in Gender.entries) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
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

        AnimatedVisibility(
            visible = showSwipeOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SwipeSideHintOverlay()
        }
    }
}

@Composable
fun SwipeSideHintOverlay() {
    Row(modifier = Modifier.fillMaxSize()) {


        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            EdgeHintCard(
                arrow = "←",
                text = "Swipe left\nto move",
                isLeft = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))


        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            EdgeHintCard(
                arrow = "→",
                text = "Swipe right\nto move",
                isLeft = false
            )
        }
    }
}
@Composable
private fun EdgeHintCard(
    arrow: String,
    text: String,
    isLeft: Boolean
) {
    val shape = RoundedCornerShape(
        topStart = if (isLeft) 0.dp else 20.dp,
        bottomStart = if (isLeft) 0.dp else 20.dp,
        topEnd = if (isLeft) 20.dp else 0.dp,
        bottomEnd = if (isLeft) 20.dp else 0.dp
    )

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .clip(shape)
            .background(Color(0xFFF7F7F9))
            .border(
                width = 2.dp,
                color = Color(0xFF3E3A59),
                shape = shape
            )
            .padding(horizontal = 10.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = arrow,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color.Black
            )
        }
    }
}



