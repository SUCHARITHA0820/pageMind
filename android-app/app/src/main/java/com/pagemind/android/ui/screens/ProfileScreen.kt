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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pagemind.android.data.remote.ApiClient
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.ui.components.BookCard
import com.pagemind.android.ui.components.PrimaryButton
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

@Composable
fun ProfileScreen(
    onNavigateEditProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateLanguage: () -> Unit,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    onBookClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("PageMind Reader") }
    var email by remember { mutableStateOf("reader@pagemind.com") }
    var phone by remember { mutableStateOf("+1 (555) 019-2834") }
    var dob by remember { mutableStateOf("1995-08-15") }
    var profilePicUrl by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Prefer not to say") }
    var likedBooks by remember { mutableStateOf<List<BookDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSavedSuccess by remember { mutableStateOf(false) }
    var imgError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fetch User Profile and Liked Books
        coroutineScope.launch {
            try {
                val apiService = ApiClient.getApiService(context)
                val userResp = apiService.getProfile()
                if (userResp.isSuccessful && userResp.body() != null) {
                    val u = userResp.body()!!
                    fullName = u.name ?: fullName
                    email = u.email ?: email
                    if (!u.phoneNumber.isNullOrBlank()) phone = u.phoneNumber
                    if (!u.dob.isNullOrBlank()) dob = u.dob
                    if (!u.profilePicUrl.isNullOrBlank()) profilePicUrl = u.profilePicUrl
                    if (!u.gender.isNullOrBlank()) gender = u.gender
                }

                val likesResp = apiService.getUserLikes()
                if (likesResp.isSuccessful && likesResp.body() != null) {
                    likedBooks = likesResp.body()!!
                }
            } catch (e: Exception) {
                // Keep default state
            }
        }
    }

    val saveProfile = {
        coroutineScope.launch {
            isLoading = true
            isSavedSuccess = false
            try {
                // Simulate save
                kotlinx.coroutines.delay(500)
                isSavedSuccess = true
            } catch (e: Exception) {
                // Ignore
            } finally {
                isLoading = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1. Header Row
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
                    text = "User Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Manage your personal information, reading preferences, and liked books.",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(start = 48.dp, bottom = 20.dp)
            )
        }

        // 2. Profile Details Glass Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar with Fallback
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(BgSurface)
                            .border(2.dp, PrimaryViolet, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val validPicUrl = profilePicUrl.trim()
                        if (validPicUrl.isNotEmpty() && !imgError) {
                            AsyncImage(
                                model = validPicUrl,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                onError = { imgError = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "👤", fontSize = 48.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reset Profile Picture Button
                    if (profilePicUrl.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1AFFFFFF))
                                .clickable {
                                    profilePicUrl = ""
                                    imgError = false
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset to Contact Emoji 👤", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = fullName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Text(
                        text = email,
                        fontSize = 13.sp,
                        color = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Fields
                    OutlinedTextField(
                        value = profilePicUrl,
                        onValueChange = {
                            profilePicUrl = it
                            imgError = false
                        },
                        label = { Text("Profile Picture Image URL") },
                        placeholder = { Text("Paste image URL (https://...)", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = if (isLoading) "Saving..." else if (isSavedSuccess) "Saved Successfully! ✓" else "Save Profile Changes",
                        onClick = { saveProfile() },
                        leadingIcon = Icons.Default.Edit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Navigation Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileMenuRow(icon = Icons.Default.Settings, title = "App Settings", onClick = onNavigateSettings)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Liked Books Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "My Liked Books (${likedBooks.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
            }
        }

        if (likedBooks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No liked books yet. Explore the home catalog to add some to your favorites!",
                            color = TextMuted,
                            fontSize = 13.sp
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
                        isLiked = true
                    )
                }
            }
        }

        // 5. Logout Button
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFA5B4FC))
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
