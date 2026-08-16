import pymysql
import urllib.request
import json
import urllib.parse
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'Suchi@520',
    'database': 'pagemind',
    'autocommit': True
}

def fetch_cover_url(title, author):
    clean_title = title.split('(')[0].split(':')[0].strip()
    clean_author = (author or '').split(',')[0].strip()

    # Try query with title and author first
    q = urllib.parse.quote(f"{clean_title} {clean_author}".strip())
    url = f"https://openlibrary.org/search.json?q={q}&limit=3"
    headers = {'User-Agent': 'PageMindApp/2.0 (contact@pagemind.com)'}

    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            docs = data.get('docs', [])
            for doc in docs:
                cover_i = doc.get('cover_i')
                if cover_i and isinstance(cover_i, int) and cover_i > 0:
                    return f"https://covers.openlibrary.org/b/id/{cover_i}-L.jpg"
                isbns = doc.get('isbn', [])
                if isbns and len(isbns) > 0:
                    return f"https://covers.openlibrary.org/b/isbn/{isbns[0]}-L.jpg"
    except Exception:
        pass

    # Try title only if combined query returned no cover
    q_title = urllib.parse.quote(clean_title)
    url_title = f"https://openlibrary.org/search.json?title={q_title}&limit=3"
    try:
        req = urllib.request.Request(url_title, headers=headers)
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            docs = data.get('docs', [])
            for doc in docs:
                cover_i = doc.get('cover_i')
                if cover_i and isinstance(cover_i, int) and cover_i > 0:
                    return f"https://covers.openlibrary.org/b/id/{cover_i}-L.jpg"
    except Exception:
        pass

    return None

def process_book(book):
    book_id, title, author = book
    cover_url = fetch_cover_url(title, author)
    
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        if cover_url:
            cursor.execute("UPDATE books SET cover_url = %s WHERE id = %s", (cover_url, book_id))
        else:
            cursor.execute("UPDATE books SET cover_url = NULL WHERE id = %s AND (cover_url LIKE '%%placehold%%' OR cover_url = '')", (book_id,))
        conn.close()
    except Exception as e:
        print(f"Error updating book {book_id}: {e}", flush=True)

    time.sleep(0.1)
    return book_id, title, cover_url

def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    # First, convert any placehold.co entries to NULL
    cursor.execute("UPDATE books SET cover_url = NULL WHERE cover_url LIKE '%placehold%';")
    print("Cleared placehold.co URLs from database.", flush=True)
    
    cursor.execute("SELECT id, title, author FROM books WHERE cover_url IS NULL ORDER BY id ASC")
    books = cursor.fetchall()
    conn.close()

    total = len(books)
    print(f"Found {total} books needing real cover URLs.", flush=True)

    if total == 0:
        print("All books already have real cover URLs!")
        return

    updated_real = 0
    processed = 0

    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = {executor.submit(process_book, b): b for b in books}
        for future in as_completed(futures):
            book_id, title, cover_url = future.result()
            processed += 1
            if cover_url:
                updated_real += 1
            if processed % 10 == 0 or processed == total:
                print(f"[{processed}/{total}] {updated_real} real covers found and updated.", flush=True)

    print(f"\nDone! Updated {updated_real}/{total} books with real cover URLs.", flush=True)

if __name__ == '__main__':
    main()
