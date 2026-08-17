package com.pagemind.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pagemind.android.data.remote.BookDto
import com.pagemind.android.ui.theme.AccentCyan
import com.pagemind.android.ui.theme.BgSurface
import com.pagemind.android.ui.theme.BorderSubtle
import com.pagemind.android.ui.theme.CardBg
import com.pagemind.android.ui.theme.CardBorder
import com.pagemind.android.ui.theme.PrimaryGradient
import com.pagemind.android.ui.theme.PrimaryViolet
import com.pagemind.android.ui.theme.TextMain
import com.pagemind.android.ui.theme.TextMuted
import kotlin.math.abs

// --- Gradient Palettes matching web BookCover.jsx ---
private val GRADIENT_PALETTES = listOf(
    listOf(Color(0xFF312E81), Color(0xFF1E1B4B), Color(0xFF0F172A)),
    listOf(Color(0xFF1E3A8A), Color(0xFF172554), Color(0xFF0F172A)),
    listOf(Color(0xFF4C1D95), Color(0xFF2E1065), Color(0xFF0F172A)),
    listOf(Color(0xFF701A75), Color(0xFF4A044E), Color(0xFF0F172A)),
    listOf(Color(0xFF831843), Color(0xFF500724), Color(0xFF0F172A)),
    listOf(Color(0xFF064E3B), Color(0xFF022C22), Color(0xFF0F172A)),
    listOf(Color(0xFF164E63), Color(0xFF083344), Color(0xFF0F172A))
)

private fun getHashIndex(title: String, author: String): Int {
    val str = title + author
    var hash = 0
    for (char in str) {
        hash = (hash shl 5) - hash + char.code
    }
    return abs(hash) % GRADIENT_PALETTES.size
}

/**
 * 1. BookCover Composable
 * Matches web-frontend's BookCover.jsx component 1:1.
 * Renders cover image with Coil, falling back to gradient palette + book icon badge + title/author text when image URL is missing or fails.
 */
@Composable
fun BookCover(
    coverUrl: String?,
    title: String,
    author: String,
    modifier: Modifier = Modifier,
    width: Dp = 105.dp,
    height: Dp = 155.dp,
    borderRadius: Dp = 10.dp,
    showTitleFallback: Boolean = true
) {
    val context = LocalContext.current
    var imgError by remember(coverUrl) { mutableStateOf(false) }

    val cleanUrl = coverUrl?.trim()

    val isValidUrl = !cleanUrl.isNullOrBlank() &&
            cleanUrl != "null" &&
            cleanUrl != "undefined" &&
            !cleanUrl.contains("placeholder") &&
            !cleanUrl.contains("placehold.co")

    val gradientColors = GRADIENT_PALETTES[getHashIndex(title, author)]
    val gradientBrush = Brush.linearGradient(colors = gradientColors)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(borderRadius))
            .background(gradientBrush)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(borderRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (isValidUrl && !imgError) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(cleanUrl)
                    .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36")
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onError = { imgError = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
                if (!author.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = author,
                        fontSize = 8.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 2. BookCard (Horizontal Glass Card)
 * Replicates web-frontend's Home.jsx horizontal book card 1:1.
 */
@Composable
fun BookCard(
    book: BookDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    onLikeToggle: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Book Cover Image with Fallback Gradient
            BookCover(
                coverUrl = book.coverUrl,
                title = book.title,
                author = book.author,
                width = 105.dp,
                height = 155.dp,
                borderRadius = 10.dp
            )

            // Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(155.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Genre Badge
                        if (!book.genre.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0x266366F1), shape = RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0x4D6366F1), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = book.genre ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Heart Button
                        if (onLikeToggle != null) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiked) Color(0x33EC4899) else Color(0x0DFFFFFF))
                                    .border(1.dp, if (isLiked) Color(0xFFEC4899) else CardBorder, CircleShape)
                                    .clickable { onLikeToggle() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) Color(0xFFEC4899) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = book.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "by ${book.author}",
                        fontSize = 12.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!book.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.description ?: "",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Footer Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLiked) "Liked" else "Like",
                        fontSize = 12.sp,
                        color = if (isLiked) Color(0xFFEC4899) else TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "View →",
                        fontSize = 12.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 3. BookCardGridItem
 * Vertical card composable for grid views (e.g., Liked Books section in Profile).
 */
@Composable
fun BookCardGridItem(
    book: BookDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            BookCover(
                coverUrl = book.coverUrl,
                title = book.title,
                author = book.author,
                width = Dp.Unspecified,
                height = 180.dp,
                borderRadius = 12.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = book.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "by ${book.author}",
                fontSize = 12.sp,
                color = AccentCyan,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!book.genre.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x266366F1), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x4D6366F1), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = book.genre ?: "General",
                            fontSize = 10.sp,
                            color = Color(0xFFA5B4FC),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${book.averageRating ?: 4.5}",
                        fontSize = 11.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 4. PrimaryButton
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                brush = PrimaryGradient,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 5. PageMindTextField
 */
@Composable
fun PageMindTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder != null) { { Text(placeholder, color = TextMuted) } } else null,
            leadingIcon = if (leadingIcon != null) { { Icon(leadingIcon, contentDescription = null, tint = TextMuted) } } else null,
            trailingIcon = trailingIcon,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgSurface,
                unfocusedContainerColor = BgSurface,
                focusedBorderColor = PrimaryViolet,
                unfocusedBorderColor = BorderSubtle,
                focusedLabelColor = PrimaryViolet,
                unfocusedLabelColor = TextMuted,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain
            )
        )
    }
}
