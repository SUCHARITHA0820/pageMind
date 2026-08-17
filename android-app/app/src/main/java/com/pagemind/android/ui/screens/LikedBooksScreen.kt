package com.pagemind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagemind.android.data.local.FallbackBooks
import com.pagemind.android.data.remote.ApiClient
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.ui.components.BookCard
import com.pagemind.android.ui.theme.BgDark
import com.pagemind.android.ui.theme.CardBg
import com.pagemind.android.ui.theme.CardBorder
import com.pagemind.android.ui.theme.PrimaryViolet
import com.pagemind.android.ui.theme.TextMain
import com.pagemind.android.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun LikedBooksScreen(
    onBookClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var likedBooks by remember { mutableStateOf<List<BookDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val fetchLikedBooks = {
        coroutineScope.launch {
            isLoading = true
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getUserLikes()
                if (response.isSuccessful && response.body() != null) {
                    likedBooks = response.body()!!
                } else {
                    throw Exception("Failed to fetch user likes")
                }
            } catch (e: Exception) {
                likedBooks = FallbackBooks.FALLBACK_BOOKS.take(3)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchLikedBooks()
    }

    val toggleLike = { bookId: Long ->
        likedBooks = likedBooks.filter { it.id != bookId }
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                apiService.unlikeBook(bookId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Top Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextMain)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEC4899))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Liked Books (${likedBooks.size})",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryViolet)
                }
            }
        } else if (likedBooks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EC4899))
                                .border(1.dp, Color(0xFFEC4899), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No liked books yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Browse the catalog on the Home screen and tap the heart icon on any book to add it to your favorites.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(likedBooks) { book ->
                Box(modifier = Modifier.padding(vertical = 6.dp)) {
                    BookCard(
                        book = book,
                        onClick = { onBookClick(book.id) },
                        isLiked = true,
                        onLikeToggle = { toggleLike(book.id) }
                    )
                }
            }
        }
    }
}
