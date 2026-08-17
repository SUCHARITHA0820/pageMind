package com.pagemind.android.data.local

import com.pagemind.android.data.remote.BookDto

object FallbackBooks {

    private val GENRES = listOf(
        "Fiction", "Science Fiction", "Romance", "Fantasy", "Mystery",
        "Non-Fiction", "Self-Help", "Classic Literature", "Thriller",
        "Dystopian", "Biography", "History", "Poetry", "Graphic Novel",
        "Young Adult", "Horror", "Philosophy"
    )

    private val COVER_URLS = listOf(
        "https://covers.openlibrary.org/b/id/8225266-L.jpg",
        "https://covers.openlibrary.org/b/id/8745958-L.jpg",
        "https://covers.openlibrary.org/b/id/14348537-L.jpg",
        "https://covers.openlibrary.org/b/id/7222246-L.jpg",
        "https://covers.openlibrary.org/b/id/9273490-L.jpg",
        "https://covers.openlibrary.org/b/id/8100921-L.jpg",
        "https://covers.openlibrary.org/b/id/8406786-L.jpg",
        "https://covers.openlibrary.org/b/id/10521270-L.jpg",
        "https://covers.openlibrary.org/b/id/12889269-L.jpg",
        "https://covers.openlibrary.org/b/id/12539702-L.jpg",
        "https://covers.openlibrary.org/b/id/10006520-L.jpg",
        "https://covers.openlibrary.org/b/id/7984916-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780141439518-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780316769488-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780060850524-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9781594631931-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780156027328-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9781400033416-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780375842207-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9781451673319-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780307387899-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780385333849-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780141441146-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780141439556-L.jpg",
        "https://covers.openlibrary.org/b/isbn/9780399501487-L.jpg"
    )

    private fun generate5000Books(): List<BookDto> {
        val list = mutableListOf<BookDto>()

        val seed12 = listOf(
            BookDto(1L, "To Kill a Mockingbird", "Harper Lee", "Fiction", "A compelling story of racial injustice and the destruction of innocence in the American South.", "https://covers.openlibrary.org/b/id/8225266-L.jpg", 4.8, 1960),
            BookDto(2L, "1984", "George Orwell", "Science Fiction", "A dystopian vision of a totalitarian regime where Big Brother is always watching.", "https://covers.openlibrary.org/b/id/8745958-L.jpg", 4.8, 1949),
            BookDto(3L, "Pride and Prejudice", "Jane Austen", "Romance", "A classic romance exploring manners, upbringing, and marriage in 19th century England.", "https://covers.openlibrary.org/b/id/14348537-L.jpg", 4.7, 1813),
            BookDto(4L, "The Great Gatsby", "F. Scott Fitzgerald", "Classic Literature", "A tragic tale of ambition, love, and the American Dream in the Roaring Twenties.", "https://covers.openlibrary.org/b/id/7222246-L.jpg", 4.5, 1925),
            BookDto(5L, "The Catcher in the Rye", "J.D. Salinger", "Fiction", "A story of teenage rebellion, alienation, and identity in post-WWII America.", "https://covers.openlibrary.org/b/id/9273490-L.jpg", 4.2, 1951),
            BookDto(6L, "Dune", "Frank Herbert", "Science Fiction", "An epic sci-fi saga of politics, religion, and survival on the desert planet Arrakis.", "https://covers.openlibrary.org/b/id/8100921-L.jpg", 4.9, 1965),
            BookDto(7L, "The Hobbit", "J.R.R. Tolkien", "Fantasy", "Bilbo Baggins journeys to victory and treasure in Middle-earth.", "https://covers.openlibrary.org/b/id/8406786-L.jpg", 4.9, 1937),
            BookDto(8L, "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", "An orphaned boy discovers he is a wizard and attends Hogwarts School of Witchcraft and Wizardry.", "https://covers.openlibrary.org/b/id/10521270-L.jpg", 4.9, 1997),
            BookDto(9L, "Atomic Habits", "James Clear", "Self-Help", "An easy & proven way to build good habits & break bad ones.", "https://covers.openlibrary.org/b/id/12889269-L.jpg", 4.8, 2018),
            BookDto(10L, "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "Non-Fiction", "A groundbreaking narrative of humanity’s creation and evolution.", "https://covers.openlibrary.org/b/id/12539702-L.jpg", 4.7, 2011),
            BookDto(11L, "The Alchemist", "Paulo Coelho", "Fiction", "An inspiring fable about an Andalusian shepherd boy following his dreams.", "https://covers.openlibrary.org/b/id/10006520-L.jpg", 4.6, 1988),
            BookDto(12L, "Sherlock Holmes: Complete Novels", "Arthur Conan Doyle", "Mystery", "The iconic detective solves baffling mysteries across Victorian London.", "https://covers.openlibrary.org/b/id/7984916-L.jpg", 4.8, 1887)
        )
        list.addAll(seed12)

        val prefixes = listOf("The Secret of", "Echoes of", "Chronicles of", "The Lost", "Shadows over", "Beyond the", "Whispers of", "Tales from", "The Last", "Journey to", "Legacy of", "Rise of", "Path of", "Mastering", "Art of", "Voices of", "Realm of", "The Age of")
        val nouns = listOf("Eternity", "Silence", "Destiny", "Stars", "Midnight", "Solitude", "Avalon", "Veritas", "Horizon", "Shadows", "Illusion", "Kingdom", "Wisdom", "Infinity", "Empires", "Darkness", "Light", "Memories")
        val authorsList = listOf("Arthur C. Clarke", "Brandon Sanderson", "Agatha Christie", "Stephen King", "Virginia Woolf", "Isaac Asimov", "Gabriel García Márquez", "Toni Morrison", "Ernest Hemingway", "Haruki Murakami", "Ursula K. Le Guin", "Philip K. Dick", "Cormac McCarthy", "Neil Gaiman", "Maya Angelou", "Malcolm Gladwell")

        for (i in 13..5000) {
            val genreIndex = (i - 1) % GENRES.size
            val genre = GENRES[genreIndex]
            val prefix = prefixes[(i * 7) % prefixes.size]
            val noun = nouns[(i * 13) % nouns.size]
            val author = authorsList[(i * 11) % authorsList.size]
            val coverUrl = COVER_URLS[(i - 1) % COVER_URLS.size]
            val rating = 3.5 + ((i % 16) * 0.1)
            val year = 1920 + (i % 105)

            list.add(
                BookDto(
                    id = i.toLong(),
                    title = "$prefix $noun Vol. ${((i - 13) / 100) + 1}",
                    author = author,
                    genre = genre,
                    description = "An extraordinary $genre masterpiece detailing $prefix $noun with compelling characters and deep storytelling.",
                    coverUrl = coverUrl,
                    averageRating = (rating * 10).toInt() / 10.0,
                    publicationYear = year
                )
            )
        }
        return list
    }

    val FALLBACK_BOOKS: List<BookDto> by lazy {
        generate5000Books()
    }

    fun getFallbackBooks(genre: String? = null, search: String? = null): List<BookDto> {
        var filtered = FALLBACK_BOOKS
        if (!genre.isNullOrBlank()) {
            val targetGenres = genre.split(",", "/", "&").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            filtered = filtered.filter { book ->
                val bg = book.genre?.lowercase() ?: ""
                targetGenres.any { tg -> bg.contains(tg) || tg.contains(bg) }
            }
        }
        if (!search.isNullOrBlank()) {
            val q = search.lowercase()
            filtered = filtered.filter { book ->
                book.title.lowercase().contains(q) ||
                book.author.lowercase().contains(q) ||
                (book.genre?.lowercase()?.contains(q) == true)
            }
        }
        if (filtered.isEmpty()) {
            return FALLBACK_BOOKS
        }
        return filtered
    }

    fun getBooksForPrompt(prompt: String): List<BookDto> {
        val q = prompt.lowercase()

        val genreKeywords = mapOf(
            "happy" to listOf("Fantasy", "Romance", "Young Adult"),
            "joy" to listOf("Fantasy", "Romance"),
            "excited" to listOf("Fantasy", "Science Fiction", "Thriller"),
            "adventure" to listOf("Fantasy", "Science Fiction", "Fiction"),
            "sad" to listOf("Poetry", "Classic Literature", "Fiction"),
            "depressed" to listOf("Self-Help", "Poetry", "Philosophy"),
            "lonely" to listOf("Fiction", "Philosophy"),
            "anxious" to listOf("Self-Help", "Philosophy", "Non-Fiction"),
            "calm" to listOf("Self-Help", "Philosophy", "Poetry"),
            "relax" to listOf("Self-Help", "Romance"),
            "mystery" to listOf("Mystery", "Thriller"),
            "crime" to listOf("Mystery", "Thriller"),
            "scary" to listOf("Horror", "Thriller"),
            "spooky" to listOf("Horror"),
            "space" to listOf("Science Fiction"),
            "future" to listOf("Science Fiction", "Dystopian"),
            "history" to listOf("History", "Biography")
        )

        val matchedGenres = mutableListOf<String>()
        for ((key, genres) in genreKeywords) {
            if (q.contains(key)) {
                matchedGenres.addAll(genres)
            }
        }

        if (matchedGenres.isNotEmpty()) {
            val candidates = FALLBACK_BOOKS.filter { book ->
                book.genre != null && matchedGenres.any { mg -> book.genre.equals(mg, ignoreCase = true) }
            }
            if (candidates.isNotEmpty()) {
                val hash = Math.abs(q.hashCode())
                return (0 until 3).map { idx ->
                    candidates[(hash + idx * 7) % candidates.size]
                }
            }
        }

        val hash = Math.abs(q.hashCode())
        return (0 until 3).map { idx ->
            FALLBACK_BOOKS[(hash + idx * 13) % FALLBACK_BOOKS.size]
        }
    }
}
