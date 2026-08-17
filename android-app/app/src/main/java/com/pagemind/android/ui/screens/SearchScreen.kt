package com.pagemind.android.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
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
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagemind.android.data.local.FallbackBooks
import com.pagemind.android.data.remote.ApiClient
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.ui.components.BookCover
import com.pagemind.android.ui.theme.AccentCyan
import com.pagemind.android.ui.theme.BgDark
import com.pagemind.android.ui.theme.BgSurface
import com.pagemind.android.ui.theme.BorderSubtle
import com.pagemind.android.ui.theme.CardBg
import com.pagemind.android.ui.theme.CardBorder
import com.pagemind.android.ui.theme.PrimaryViolet
import com.pagemind.android.ui.theme.TextMain
import com.pagemind.android.ui.theme.TextMuted
import kotlinx.coroutines.launch

private val ALL_GENRES = listOf(
    "All Genres", "Fiction", "Science Fiction", "Romance", "Fantasy",
    "Mystery", "Non-Fiction", "Self-Help", "Classic Literature", "Thriller",
    "Dystopian", "Biography", "History", "Poetry", "Graphic Novel",
    "Young Adult", "Horror", "Philosophy"
)

@Composable
fun SearchScreen(
    onBookClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All Genres") }
    var books by remember { mutableStateOf<List<BookDto>>(emptyList()) }
    var likedStateMap = remember { mutableStateMapOf<Long, Boolean>() }
    var isLoading by remember { mutableStateOf(true) }

    val fetchSearchResults = {
        coroutineScope.launch {
            isLoading = true
            try {
                val apiService = ApiClient.getApiService(context)
                val genreFilter = if (selectedGenre == "All Genres") null else selectedGenre
                val searchFilter = if (searchQuery.isBlank()) null else searchQuery

                val response = apiService.getBooks(
                    page = 0,
                    size = 500,
                    genre = genreFilter,
                    search = searchFilter
                )

                if (response.isSuccessful && response.body() != null) {
                    books = response.body()!!.content
                } else {
                    throw Exception("Search failed")
                }
            } catch (e: Exception) {
                val genreFilter = if (selectedGenre == "All Genres") null else selectedGenre
                books = FallbackBooks.getFallbackBooks(genreFilter, searchQuery)
            } finally {
                isLoading = false
            }
        }
    }

    val fetchUserLikes = {
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val response = apiService.getUserLikes()
                if (response.isSuccessful && response.body() != null) {
                    likedStateMap.clear()
                    response.body()!!.forEach { b ->
                        likedStateMap[b.id] = true
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(searchQuery, selectedGenre) {
        fetchSearchResults()
    }

    LaunchedEffect(Unit) {
        fetchUserLikes()
    }

    val toggleLike = { bookId: Long ->
        val currentLiked = likedStateMap[bookId] == true
        likedStateMap[bookId] = !currentLiked
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                if (currentLiked) {
                    apiService.unlikeBook(bookId)
                } else {
                    apiService.likeBook(bookId)
                }
            } catch (e: Exception) {
                likedStateMap[bookId] = currentLiked
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
        // 1. Top Bar & Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextMain)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search Books Catalog",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Explore all books by title, author, or genre across our full catalog.",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
            )
        }

        // 2. Search Input Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by book title or author name...", fontSize = 14.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA5B4FC)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgSurface,
                    unfocusedContainerColor = BgSurface,
                    focusedBorderColor = PrimaryViolet,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Genre Filter Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ALL_GENRES) { genre ->
                    val isSelected = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryViolet else BgSurface)
                            .border(1.dp, if (isSelected) PrimaryViolet else CardBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedGenre = genre }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = genre,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. Results Header
        item {
            val titleText = if (selectedGenre != "All Genres") {
                "Genre: \"$selectedGenre\" (${books.size} books)"
            } else if (searchQuery.isNotBlank()) {
                "Search Results for \"$searchQuery\" (${books.size} books)"
            } else {
                "All Catalog Books (${books.size} books)"
            }

            Text(
                text = titleText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        // 5. Loading, Empty State, or Results
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
        } else if (books.isEmpty()) {
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
                                .background(Color(0x26EF4444))
                                .border(1.dp, Color(0x4DEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No matching books found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "We couldn't find any books matching your criteria. Try selecting a different genre or search phrase.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(books) { book ->
                val isLiked = likedStateMap[book.id] == true
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable { onBookClick(book.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            BookCover(
                                coverUrl = book.coverUrl,
                                title = book.title,
                                author = book.author,
                                width = 56.dp,
                                height = 76.dp,
                                borderRadius = 8.dp,
                                showTitleFallback = false
                            )

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = book.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!book.genre.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0x266366F1), shape = RoundedCornerShape(10.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = book.genre ?: "", fontSize = 10.sp, color = Color(0xFFA5B4FC), fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Text(
                                    text = "by ${book.author}",
                                    fontSize = 12.sp,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Medium
                                )

                                if (!book.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = book.description ?: "",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiked) Color(0x33EC4899) else Color(0x0DFFFFFF))
                                    .border(1.dp, if (isLiked) Color(0xFFEC4899) else CardBorder, CircleShape)
                                    .clickable { toggleLike(book.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) Color(0xFFEC4899) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
