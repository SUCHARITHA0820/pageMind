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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pagemind.android.data.local.FallbackBooks
import com.pagemind.android.data.remote.ApiClient
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.ui.components.BookCard
import com.pagemind.android.ui.theme.AccentCyan
import com.pagemind.android.ui.theme.BgDark
import com.pagemind.android.ui.theme.BgSurface
import com.pagemind.android.ui.theme.BorderSubtle
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
fun HomeScreen(
    onBookClick: (Long) -> Unit,
    onNavigateSearch: () -> Unit,
    onNavigateChatbot: () -> Unit,
    onNavigateLiked: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var books by remember { mutableStateOf<List<BookDto>>(emptyList()) }
    var likedStateMap = remember { mutableStateMapOf<Long, Boolean>() }
    var selectedGenre by remember { mutableStateOf("All Genres") }
    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(0) }
    var pageSize by remember { mutableStateOf(20) }
    var totalPages by remember { mutableStateOf(1) }
    var totalElements by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Books from API / Fallback
    val fetchBooks = {
        coroutineScope.launch {
            isLoading = true
            try {
                val apiService = ApiClient.getApiService(context)
                val genreFilter = if (selectedGenre == "All Genres") null else selectedGenre
                val searchFilter = if (searchQuery.isBlank()) null else searchQuery

                val response = apiService.getBooks(
                    page = currentPage,
                    size = pageSize,
                    genre = genreFilter,
                    search = searchFilter
                )

                if (response.isSuccessful && response.body() != null) {
                    val pageData = response.body()!!
                    books = pageData.content
                    totalElements = pageData.totalElements.toInt()
                    totalPages = pageData.totalPages
                } else {
                    throw Exception("API call non-successful")
                }
            } catch (e: Exception) {
                // Fallback to local 5000-book catalog
                val genreFilter = if (selectedGenre == "All Genres") null else selectedGenre
                val searchFilter = if (searchQuery.isBlank()) null else searchQuery
                
                val allFiltered = FallbackBooks.getFallbackBooks(genreFilter, searchFilter)
                totalElements = allFiltered.size
                totalPages = maxOf(1, (totalElements + pageSize - 1) / pageSize)
                
                val fromIndex = (currentPage * pageSize).coerceAtMost(totalElements)
                val toIndex = ((currentPage + 1) * pageSize).coerceAtMost(totalElements)
                
                books = if (fromIndex < toIndex) allFiltered.subList(fromIndex, toIndex) else emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // Fetch User Likes
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

    LaunchedEffect(currentPage, selectedGenre, searchQuery) {
        fetchBooks()
    }

    LaunchedEffect(Unit) {
        fetchUserLikes()
    }

    // Like Toggle Handler
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BgSurface,
                contentColor = TextMain
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryViolet)
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateSearch,
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateChatbot,
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant") },
                    label = { Text("AI Assistant") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateLiked,
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Liked") },
                    label = { Text("Liked") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Explore PageMind Book Catalog",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Discover 5000+ curated titles across 15 genres, powered by AI mood recommendation.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            currentPage = 0
                        },
                        placeholder = { Text("Search books by title, author, or genre...", fontSize = 13.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA5B4FC)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
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

                    // Genre Filter Pills
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
                                    .clickable {
                                        selectedGenre = genre
                                        currentPage = 0
                                    }
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
                }
            }

            // 2. Catalog Status Bar
            item {
                val startRange = if (totalElements == 0) 0 else currentPage * pageSize + 1
                val endRange = minOf((currentPage + 1) * pageSize, totalElements)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing $startRange-$endRange of $totalElements books",
                        fontSize = 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 3. Loading State or Book Items
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No books found matching your filter criteria.",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(books) { book ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book.id) },
                            isLiked = likedStateMap[book.id] == true,
                            onLikeToggle = { toggleLike(book.id) }
                        )
                    }
                }
            }

            // 4. Numbered Pagination Controls
            if (!isLoading && totalPages > 1) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable(enabled = currentPage > 0) { currentPage-- }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextMain, modifier = Modifier.size(18.dp))
                                Text("Prev", fontSize = 13.sp, color = TextMain, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Page ${currentPage + 1} of $totalPages",
                            fontSize = 13.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Next Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable(enabled = currentPage < totalPages - 1) { currentPage++ }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Next", fontSize = 13.sp, color = TextMain, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMain, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
