package com.pagemind.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagemind.android.data.local.FallbackBooks
import com.pagemind.android.data.remote.ApiClient
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.data.remote.ChatRequest
import com.pagemind.android.ui.components.BookCover
import com.pagemind.android.ui.theme.AccentCyan
import com.pagemind.android.ui.theme.BgDark
import com.pagemind.android.ui.theme.BgSurface
import com.pagemind.android.ui.theme.BorderSubtle
import com.pagemind.android.ui.theme.CardBg
import com.pagemind.android.ui.theme.CardBorder
import com.pagemind.android.ui.theme.PrimaryGradient
import com.pagemind.android.ui.theme.PrimaryViolet
import com.pagemind.android.ui.theme.TextMain
import com.pagemind.android.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.util.UUID

data class RetailerLink(
    val title: String,
    val amazonUrl: String,
    val flipkartUrl: String
)

data class ChatMessageItem(
    val id: Long,
    val sender: String, // "user" or "agent"
    val text: String,
    val mood: String? = null,
    val genre: String? = null,
    val books: List<BookDto> = emptyList(),
    val retailerLinks: List<RetailerLink> = emptyList()
)

@Composable
fun ChatbotScreen(
    initialPrompt: String? = null,
    onBookClick: (Long) -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val likedBookIds = remember { mutableStateListOf<Long>() }
    val sessionId = remember { UUID.randomUUID().toString() }

    val messages = remember {
        mutableStateListOf(
            ChatMessageItem(
                id = 1L,
                sender = "agent",
                text = "Hello! I am your PageMind AI Companion. Tell me how you are feeling or what mood of book you are looking for today!",
                mood = null,
                genre = null,
                books = emptyList(),
                retailerLinks = emptyList()
            )
        )
    }

    // Fetch initial user likes
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val resp = apiService.getUserLikes()
                if (resp.isSuccessful && resp.body() != null) {
                    likedBookIds.clear()
                    likedBookIds.addAll(resp.body()!!.map { it.id })
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Auto send initial prompt if passed from BookDetailsScreen
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputText = "Let's discuss '$initialPrompt'"
        }
    }

    // Auto scroll thread to bottom on message change or loading state
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (inputText.isBlank() || isLoading) return
        val userText = inputText.trim()
        inputText = ""

        // Add user message
        messages.add(
            ChatMessageItem(
                id = System.currentTimeMillis(),
                sender = "user",
                text = userText
            )
        )
        isLoading = true

        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.chat(
                    ChatRequest(
                        message = userText,
                        userId = 1L,
                        sessionId = sessionId,
                        session_id = sessionId
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val recommendedBooks = if (!body.recommendedBooks.isNullOrEmpty()) {
                        body.recommendedBooks!!
                    } else if (!body.detectedGenre.isNullOrBlank()) {
                        FallbackBooks.getFallbackBooks(genre = body.detectedGenre).take(5)
                    } else {
                        FallbackBooks.getBooksForPrompt(userText)
                    }

                    val links = recommendedBooks.map { book ->
                        val query = Uri.encode(book.title)
                        RetailerLink(
                            title = book.title,
                            amazonUrl = "https://www.amazon.in/s?k=$query",
                            flipkartUrl = "https://www.flipkart.com/search?q=$query"
                        )
                    }

                    messages.add(
                        ChatMessageItem(
                            id = System.currentTimeMillis() + 1,
                            sender = "agent",
                            text = body.message ?: "Based on your input, PageMind AI analyzed your mood and mapped targeted book recommendations.",
                            mood = body.detectedGenre ?: "curious",
                            genre = body.detectedGenre ?: "General Fiction",
                            books = recommendedBooks,
                            retailerLinks = links
                        )
                    )
                } else {
                    throw Exception("API response failure")
                }
            } catch (e: Exception) {
                // Offline fallback bubble with emotion-tailored dynamic book recommendations
                val fallbackBooks = FallbackBooks.getBooksForPrompt(userText)
                val fallbackLinks = fallbackBooks.map { b ->
                    val q = Uri.encode(b.title)
                    RetailerLink(
                        title = b.title,
                        amazonUrl = "https://www.amazon.in/s?k=$q",
                        flipkartUrl = "https://www.flipkart.com/search?q=$q"
                    )
                }

                messages.add(
                    ChatMessageItem(
                        id = System.currentTimeMillis() + 1,
                        sender = "agent",
                        text = "I understand you are feeling \"$userText\". Here are carefully matched book recommendations tailored to your mood:",
                        mood = "personalized",
                        genre = "Recommended for you",
                        books = fallbackBooks,
                        retailerLinks = fallbackLinks
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleLike(bookId: Long) {
        val isLiked = likedBookIds.contains(bookId)
        if (isLiked) {
            likedBookIds.remove(bookId)
        } else {
            likedBookIds.add(bookId)
        }
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                if (isLiked) {
                    apiService.unlikeBook(bookId)
                } else {
                    apiService.likeBook(bookId)
                }
            } catch (e: Exception) {
                // Local state is maintained
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
                    }
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PageMind AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
                IconButton(onClick = onNavigateHistory) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = TextMain)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LangGraph Agent Header Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x266366F1), shape = RoundedCornerShape(30.dp))
                        .border(1.dp, Color(0x4D6366F1), RoundedCornerShape(30.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "4-Node LangGraph AI Agent Bridge",
                            fontSize = 12.sp,
                            color = Color(0xFFA5B4FC),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AI Book Recommendation Assistant",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )

                Text(
                    text = "Describe your mood or literary preferences to get instant AI-curated recommendations.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Chat Messages Card Container
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Speech Thread List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            AgentSpeechBubble(
                                message = msg,
                                likedBookIds = likedBookIds,
                                onBookClick = onBookClick,
                                onLikeToggle = { toggleLike(it) }
                            )
                        }

                        // Typing indicator bubble
                        if (isLoading) {
                            item {
                                AgentTypingBubble()
                            }
                        }
                    }

                    // Bottom Chat Form Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111425))
                            .border(width = 1.dp, color = BorderSubtle)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Ask PageMind AI...", color = TextMuted, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BgSurface,
                                    unfocusedContainerColor = BgSurface,
                                    focusedBorderColor = PrimaryViolet,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextMain,
                                    unfocusedTextColor = TextMain
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { sendMessage() },
                                enabled = !isLoading && inputText.isNotBlank(),
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(brush = PrimaryGradient, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgentSpeechBubble(
    message: ChatMessageItem,
    likedBookIds: List<Long>,
    onBookClick: (Long) -> Unit,
    onLikeToggle: (Long) -> Unit
) {
    val context = LocalContext.current
    val isUser = message.sender == "user"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isUser) {
            // User Speech Bubble
            Box(
                modifier = Modifier
                    .background(brush = PrimaryGradient, shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            // Agent Speech Bubble matching web Chatbot.jsx
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .background(Color(0xFF111425), shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                    .border(1.dp, Color(0xFF1F293D), RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                    .padding(14.dp)
            ) {
                Column {
                    // Agent Header Avatar Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(PrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PageMind AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFA5B4FC)
                        )
                    }

                    // Main Message Text
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = TextMain,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = if (message.mood != null || message.genre != null || message.books.isNotEmpty()) 10.dp else 0.dp)
                    )

                    // Mood & Genre Badges Row
                    if (message.mood != null || message.genre != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = if (message.books.isNotEmpty()) 12.dp else 0.dp)
                        ) {
                            if (message.mood != null) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x26EC4899), shape = RoundedCornerShape(20.dp))
                                        .border(1.dp, Color(0x4DEC4899), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFF472B6), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Mood: ${message.mood}", fontSize = 11.sp, color = Color(0xFFF472B6), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            if (message.genre != null) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x2606B6D4), shape = RoundedCornerShape(20.dp))
                                        .border(1.dp, Color(0x4D06B6D4), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Genre: ${message.genre}", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // Embedded Recommended Book Cards
                    if (message.books.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            message.books.forEachIndexed { idx, book ->
                                val linkObj = message.retailerLinks.getOrNull(idx)
                                val isLiked = likedBookIds.contains(book.id)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1F293D), RoundedCornerShape(12.dp))
                                        .clickable { onBookClick(book.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Cover Art
                                        BookCover(
                                            coverUrl = book.coverUrl,
                                            title = book.title,
                                            author = book.author,
                                            width = 56.dp,
                                            height = 76.dp,
                                            borderRadius = 8.dp,
                                            showTitleFallback = false
                                        )

                                        // Details
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = book.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFF8FAFC),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                // Heart Toggle Button
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isLiked) Color(0x33EC4899) else Color(0x0DFFFFFF))
                                                        .border(1.dp, if (isLiked) Color(0xFFEC4899) else CardBorder, CircleShape)
                                                        .clickable { onLikeToggle(book.id) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                        contentDescription = "Like",
                                                        tint = if (isLiked) Color(0xFFEC4899) else Color(0xFF94A3B8),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }

                                            // Author & Badges Row
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "by ${book.author}",
                                                    fontSize = 12.sp,
                                                    color = AccentCyan,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )

                                                if (book.publicationYear != null && book.publicationYear > 0) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text(text = "${book.publicationYear}", fontSize = 10.sp, color = TextMuted)
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(text = "${book.averageRating ?: 4.5}", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            if (!book.description.isNullOrBlank()) {
                                                Text(
                                                    text = book.description ?: "",
                                                    fontSize = 11.sp,
                                                    color = TextMuted,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    lineHeight = 14.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Retailer Buttons (Amazon / Flipkart)
                                            if (linkObj != null) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Amazon Button
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0x1AF59E0B), RoundedCornerShape(6.dp))
                                                            .border(1.dp, Color(0x4DF59E0B), RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkObj.amazonUrl))
                                                                context.startActivity(intent)
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Amazon", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(9.dp))
                                                        }
                                                    }

                                                    // Flipkart Button
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0x1A3B82F6), RoundedCornerShape(6.dp))
                                                            .border(1.dp, Color(0x4D3B82F6), RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkObj.flipkartUrl))
                                                                context.startActivity(intent)
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(11.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Flipkart", fontSize = 10.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.SemiBold)
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(9.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgentTypingBubble() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF111425), shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .border(1.dp, Color(0xFF1F293D), RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PageMind AI is thinking",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = AccentCyan,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
