import os
import random
import logging
from typing import Dict, Any, List

try:
    import pymysql
    import pymysql.cursors
except ImportError:
    pymysql = None

logger = logging.getLogger("ai_agent_4node")

FALLBACK_BOOKS_BY_GENRE = {
    "Romance": [
        {"title": "The Seven Husbands of Evelyn Hugo", "author": "Taylor Jenkins Reid", "genre": "Romance", "description": "A reclusive Hollywood movie icon tells her secret life story.", "published_year": 2017, "rating": 4.5},
        {"title": "Pride and Prejudice", "author": "Jane Austen", "genre": "Romance", "description": "The classic romance story of Elizabeth Bennet and Mr. Darcy.", "published_year": 1813, "rating": 4.4},
        {"title": "Beach Read", "author": "Emily Henry", "genre": "Romance", "description": "Two rival authors spend the summer writing in neighboring beach houses.", "published_year": 2020, "rating": 4.1},
        {"title": "The Notebook", "author": "Nicholas Sparks", "genre": "Romance", "description": "An enduring love story between Noah and Allie spanning decades of trial and war.", "published_year": 1996, "rating": 4.2}
    ],
    "Fiction": [
        {"title": "The Midnight Library", "author": "Matt Haig", "genre": "Fiction", "description": "Between life and death there is a library of infinite possibilities.", "published_year": 2020, "rating": 4.1},
        {"title": "The Alchemist", "author": "Paulo Coelho", "genre": "Fiction", "description": "An inspiring fable about following your dream.", "published_year": 1988, "rating": 4.3},
        {"title": "To Kill a Mockingbird", "author": "Harper Lee", "genre": "Fiction", "description": "A timeless novel about justice and empathy.", "published_year": 1960, "rating": 4.2},
        {"title": "The Great Gatsby", "author": "F. Scott Fitzgerald", "genre": "Fiction", "description": "A tragic tale of ambition, love, and the American Dream in the Roaring Twenties.", "published_year": 1925, "rating": 4.1}
    ],
    "Science Fiction": [
        {"title": "Dune", "author": "Frank Herbert", "genre": "Science Fiction", "description": "A masterwork of sand, spice, and planetary politics.", "published_year": 1965, "rating": 4.7},
        {"title": "Project Hail Mary", "author": "Andy Weir", "genre": "Science Fiction", "description": "A lone astronaut must save Earth from extinction.", "published_year": 2021, "rating": 4.6},
        {"title": "Neuromancer", "author": "William Gibson", "genre": "Science Fiction", "description": "The iconic cyberpunk thriller.", "published_year": 1984, "rating": 4.2},
        {"title": "Foundation", "author": "Isaac Asimov", "genre": "Science Fiction", "description": "Psychohistory aims to preserve human knowledge during galactic collapse.", "published_year": 1951, "rating": 4.4}
    ],
    "Fantasy": [
        {"title": "The Name of the Wind", "author": "Patrick Rothfuss", "genre": "Fantasy", "description": "The story of Kvothe, a legendary wizard and musician.", "published_year": 2007, "rating": 4.5},
        {"title": "The Way of Kings", "author": "Brandon Sanderson", "genre": "Fantasy", "description": "An epic saga of High Fantasy and Radiant knights.", "published_year": 2010, "rating": 4.7},
        {"title": "The Hobbit", "author": "J.R.R. Tolkien", "genre": "Fantasy", "description": "Bilbo Baggins' classic adventure.", "published_year": 1937, "rating": 4.8},
        {"title": "A Game of Thrones", "author": "George R.R. Martin", "genre": "Fantasy", "description": "Noble houses clash for control of the Iron Throne of Westeros.", "published_year": 1996, "rating": 4.4}
    ],
    "Mystery": [
        {"title": "The Silent Patient", "author": "Alex Michaelides", "genre": "Mystery", "description": "A psychological thriller about a woman's act of violence.", "published_year": 2019, "rating": 4.2},
        {"title": "The Girl with the Dragon Tattoo", "author": "Stieg Larsson", "genre": "Mystery", "description": "A gripping murder mystery and corporate thriller.", "published_year": 2005, "rating": 4.3},
        {"title": "And Then There Were None", "author": "Agatha Christie", "genre": "Mystery", "description": "Ten strangers trapped on an island.", "published_year": 1939, "rating": 4.6},
        {"title": "Gone Girl", "author": "Gillian Flynn", "genre": "Mystery", "description": "A husband becomes the chief suspect when his wife vanishes.", "published_year": 2012, "rating": 4.1}
    ],
    "Thriller": [
        {"title": "The Bourne Identity", "author": "Robert Ludlum", "genre": "Thriller", "description": "An amnesiac man pulled from the sea discovers he possesses lethal combat skills.", "published_year": 1980, "rating": 4.3},
        {"title": "The Firm", "author": "John Grisham", "genre": "Thriller", "description": "A young attorney discovers his high-paying law firm is a front for mob money laundering.", "published_year": 1991, "rating": 4.1},
        {"title": "Dark Matter", "author": "Blake Crouch", "genre": "Thriller", "description": "A physicist is kidnapped and thrust into an alternate reality.", "published_year": 2016, "rating": 4.4}
    ],
    "Horror": [
        {"title": "Dracula", "author": "Bram Stoker", "genre": "Horror", "description": "Count Dracula attempts to move from Transylvania to England to spread the undead curse.", "published_year": 1897, "rating": 4.1},
        {"title": "The Shining", "author": "Stephen King", "genre": "Horror", "description": "Jack Torrance takes a winter caretaker job at an isolated hotel as malevolent forces gather.", "published_year": 1977, "rating": 4.5},
        {"title": "Frankenstein", "author": "Mary Shelley", "genre": "Horror", "description": "Victor Frankenstein creates a living creature from reanimated corpses with tragic results.", "published_year": 1818, "rating": 4.0}
    ],
    "Self-Help": [
        {"title": "Atomic Habits", "author": "James Clear", "genre": "Self-Help", "description": "An easy and proven way to build good habits and break bad ones.", "published_year": 2018, "rating": 4.8},
        {"title": "Deep Work", "author": "Cal Newport", "genre": "Self-Help", "description": "Rules for focused success in a distracted world.", "published_year": 2016, "rating": 4.3},
        {"title": "Thinking, Fast and Slow", "author": "Daniel Kahneman", "genre": "Self-Help", "description": "Explores the dual systems driving human thought.", "published_year": 2011, "rating": 4.2},
        {"title": "How to Win Friends and Influence People", "author": "Dale Carnegie", "genre": "Self-Help", "description": "Timeless advice on communication, empathy, and building relationships.", "published_year": 1936, "rating": 4.6}
    ]
}

def retrieve_books_by_genres(genres: List[str]) -> List[Dict[str, Any]]:
    print(f"[Node 3: book_retriever] Genre(s) Being Queried: {genres}", flush=True)
    logger.info(f"[Node 3: book_retriever] Genre(s) Being Queried: {genres}")
    retrieved_books = []

    db_host = os.getenv("DB_HOST", "localhost")
    db_port = int(os.getenv("DB_PORT", "3306"))
    db_user = os.getenv("DB_USER", os.getenv("DB_USERNAME", "root"))
    db_pass = os.getenv("DB_PASSWORD", "Suchi@520")
    db_name = os.getenv("DB_NAME", "pagemind")

    if pymysql is not None and genres:
        try:
            connection = pymysql.connect(
                host=db_host,
                port=db_port,
                user=db_user,
                password=db_pass,
                database=db_name,
                cursorclass=pymysql.cursors.DictCursor,
                connect_timeout=3
            )
            with connection:
                with connection.cursor() as cursor:
                    placeholders = ", ".join(["%s"] * len(genres))

                    # 1. Count matching books in database before applying LIMIT/RANDOM
                    count_sql = f"SELECT COUNT(*) as cnt FROM books WHERE genre IN ({placeholders})"
                    exact_count_sql = count_sql.replace("%s", "'%s'") % tuple(genres) if len(genres) > 0 else count_sql
                    print(f"[Node 3: book_retriever] Exact SQL Query Executed (Count): '{exact_count_sql}'", flush=True)
                    logger.info(f"[Node 3: book_retriever] Exact SQL Query Executed (Count): '{exact_count_sql}'")
                    cursor.execute(count_sql, tuple(genres))
                    count_res = cursor.fetchone()
                    total_matched = count_res.get("cnt", 0) if count_res else 0
                    print(f"[Node 3: book_retriever] Number of books matched before applying LIMIT/RANDOM: {total_matched}", flush=True)
                    logger.info(f"[Node 3: book_retriever] Number of books matched before applying LIMIT/RANDOM: {total_matched}")

                    # 2. Retrieve randomized sample of 5 books matching genres
                    sql = f"SELECT id, title, author, genre, description, cover_url, buy_links_json, published_year, rating FROM books WHERE genre IN ({placeholders}) ORDER BY RAND() LIMIT 5"
                    exact_retrieval_sql = sql.replace("%s", "'%s'") % tuple(genres) if len(genres) > 0 else sql
                    print(f"[Node 3: book_retriever] Exact SQL Query Executed (Retrieval): '{exact_retrieval_sql}'", flush=True)
                    logger.info(f"[Node 3: book_retriever] Exact SQL Query Executed (Retrieval): '{exact_retrieval_sql}'")
                    cursor.execute(sql, tuple(genres))
                    rows = cursor.fetchall()
                    for row in rows:
                        retrieved_books.append({
                            "id": row.get("id"),
                            "title": row.get("title"),
                            "author": row.get("author"),
                            "genre": row.get("genre"),
                            "description": row.get("description", ""),
                            "cover_url": row.get("cover_url"),
                            "buy_links_json": row.get("buy_links_json"),
                            "published_year": row.get("published_year"),
                            "rating": float(row.get("rating")) if row.get("rating") is not None else None
                        })
            print(f"[Node 3: book_retriever] MySQL successfully returned {len(retrieved_books)} books: {[b['title'] for b in retrieved_books]}", flush=True)
        except Exception as e:
            print(f"[Node 3: book_retriever] MySQL connection error ({e}), proceeding to fallback retrieval.", flush=True)

    if not retrieved_books:
        print("[Node 3: book_retriever] Using FALLBACK book retrieval.", flush=True)
        pool = []
        for g in genres:
            pool.extend(FALLBACK_BOOKS_BY_GENRE.get(g, []))
        if not pool:
            pool = FALLBACK_BOOKS_BY_GENRE["Fiction"]

        print(f"[Node 3: book_retriever] Number of books matched in fallback pool before LIMIT: {len(pool)}", flush=True)
        random.shuffle(pool)
        for item in pool:
            if item not in retrieved_books and len(retrieved_books) < 5:
                retrieved_books.append(item)
        print(f"[Node 3: book_retriever] Fallback pool returned {len(retrieved_books)} books: {[b['title'] for b in retrieved_books]}", flush=True)

    return retrieved_books

def book_retriever_node(state: Dict[str, Any]) -> Dict[str, Any]:
    genres = state.get("genres")
    if not genres:
        genre_str = state.get("genre", "Fiction")
        genres = [g.strip() for g in genre_str.split(",")]

    print(f"\n[Node 3: book_retriever] Genre(s) Being Queried: {genres}", flush=True)
    logger.info(f"[Node 3: book_retriever] Genre(s) Being Queried: {genres}")

    books = retrieve_books_by_genres(genres)
    state["books"] = books
    print(f"[Node 3: book_retriever] Total Books Returned: {len(books)}", flush=True)
    return state
