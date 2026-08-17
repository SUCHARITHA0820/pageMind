package com.pagemind.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagemind.android.data.local.FallbackBooks
import com.pagemind.android.data.remote.ApiClient
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.ui.components.BookCover
import com.pagemind.android.ui.components.PrimaryButton
import com.pagemind.android.ui.theme.AccentCyan
import com.pagemind.android.ui.theme.BgDark
import com.pagemind.android.ui.theme.BgSurface
import com.pagemind.android.ui.theme.CardBg
import com.pagemind.android.ui.theme.CardBorder
import com.pagemind.android.ui.theme.PrimaryViolet
import com.pagemind.android.ui.theme.TextMain
import com.pagemind.android.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun BookDetailsScreen(
    bookId: Long,
    onBackClick: () -> Unit,
    onAskAiClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var book by remember { mutableStateOf<BookDto?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(bookId) {
        isLoading = true
        try {
            val apiService = ApiClient.getApiService(context)
            val response = apiService.getBookById(bookId)
            if (response.isSuccessful && response.body() != null) {
                book = response.body()
            } else {
                throw Exception("Failed to fetch book")
            }
        } catch (e: Exception) {
            book = FallbackBooks.FALLBACK_BOOKS.find { it.id == bookId }
                ?: FallbackBooks.FALLBACK_BOOKS.first()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(bookId) {
        try {
            val apiService = ApiClient.getApiService(context)
            val response = apiService.getUserLikes()
            if (response.isSuccessful && response.body() != null) {
                isLiked = response.body()!!.any { it.id == bookId }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    val toggleLike = {
        val nextLiked = !isLiked
        isLiked = nextLiked
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                if (nextLiked) {
                    apiService.likeBook(bookId)
                } else {
                    apiService.unlikeBook(bookId)
                }
            } catch (e: Exception) {
                isLiked = !nextLiked
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        } else if (book == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Book not found.", color = TextMuted, fontSize = 16.sp)
            }
        } else {
            val currentBook = book!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onBackClick() }
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextMain)
                        }
                        Text("Back to Previous Page", fontSize = 14.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isLiked) Color(0x33EC4899) else Color(0x0DFFFFFF))
                            .border(1.dp, if (isLiked) Color(0xFFEC4899) else CardBorder, CircleShape)
                            .clickable { toggleLike() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFEC4899) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Book Cover Centered Hero
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BookCover(
                        coverUrl = currentBook.coverUrl,
                        title = currentBook.title,
                        author = currentBook.author,
                        width = 200.dp,
                        height = 280.dp,
                        borderRadius = 14.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details Card Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!currentBook.genre.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x266366F1), shape = RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0x4D6366F1), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = currentBook.genre ?: "", fontSize = 12.sp, color = Color(0xFFA5B4FC), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${currentBook.averageRating ?: 4.5}", fontSize = 14.sp, color = TextMain, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentBook.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "by ${currentBook.author}",
                            fontSize = 15.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (currentBook.publicationYear != null && currentBook.publicationYear!! > 0) {
                            Text(
                                text = "Published in ${currentBook.publicationYear}",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Synopsis & Overview",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentBook.description ?: "No description available for this book.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Retailer Purchase Links
                        Text(
                            text = "Available at Online Retailers",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Amazon Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF232F3E))
                                    .border(1.dp, Color(0xFFFF9900), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val query = Uri.encode("${currentBook.title} ${currentBook.author}")
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/s?k=$query"))
                                        context.startActivity(intent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFFFF9900), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Amazon", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Flipkart Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2874F0))
                                    .border(1.dp, Color(0xFFFFE500), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val query = Uri.encode("${currentBook.title} ${currentBook.author}")
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flipkart.com/search?q=$query"))
                                        context.startActivity(intent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFFFFE500), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Flipkart", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
