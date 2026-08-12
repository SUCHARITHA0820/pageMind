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

def get_real_cover_url(title, author):
    clean_title = title.split('(')[0].split(':')[0].strip()
    clean_author = (author or '').split(',')[0].strip()
    
    q_title = urllib.parse.quote(clean_title)
    q_author = urllib.parse.quote(clean_author) if clean_author else ''
    
    if q_author:
        url = f'https://openlibrary.org/search.json?title={q_title}&author={q_author}&limit=1'
    else:
        url = f'https://openlibrary.org/search.json?title={q_title}&limit=1'
        
    headers = {'User-Agent': 'PageMindApp/1.0 (contact@pagemind.com)'}
    
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=1.5) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            docs = data.get('docs', [])
            if docs and docs[0].get('cover_i'):
                cover_id = docs[0]['cover_i']
                return f'https://covers.openlibrary.org/b/id/{cover_id}-L.jpg', True
    except Exception:
        pass

    # Fallback to title-only search if title+author had no cover_i
    if q_author:
        url_title_only = f'https://openlibrary.org/search.json?title={q_title}&limit=1'
        try:
            req = urllib.request.Request(url_title_only, headers=headers)
            with urllib.request.urlopen(req, timeout=1.5) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                docs = data.get('docs', [])
                if docs and docs[0].get('cover_i'):
                    cover_id = docs[0]['cover_i']
                    return f'https://covers.openlibrary.org/b/id/{cover_id}-L.jpg', True
        except Exception:
            pass

    # Fallback styled placeholder URL when no match is found
    encoded_title = urllib.parse.quote(title)
    fallback_url = f"https://placehold.co/300x450/1a1a2e/white?text={encoded_title}"
    return fallback_url, False

def process_single_book(book):
    book_id, title, author = book
    cover_url, is_real = get_real_cover_url(title, author)
    
    # Update MySQL immediately
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("UPDATE books SET cover_url = %s WHERE id = %s", (cover_url, book_id))
        conn.close()
    except Exception as e:
        print(f"Error updating book {book_id}: {e}", flush=True)
        
    # 200ms delay per thread to prevent rate limit spikes
    time.sleep(0.2)
    return book_id, title, cover_url, is_real

def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    cursor.execute("SELECT id, title, author FROM books ORDER BY id ASC")
    books = cursor.fetchall()
    conn.close()
    
    total = len(books)
    print(f"Loaded {total} books from pagemind database.", flush=True)
    print("Fetching real cover URLs from Open Library Search API with rate limiting and fallback placeholders...", flush=True)
    
    real_found = 0
    processed = 0
    results = []
    
    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = {executor.submit(process_single_book, b): b for b in books}
        for future in as_completed(futures):
            book_id, title, cover_url, is_real = future.result()
            results.append((book_id, title, cover_url))
            processed += 1
            if is_real:
                real_found += 1
                
            if processed % 20 == 0 or processed == total:
                print(f"{processed}/{total} processed, {real_found} real covers found", flush=True)
                
    results.sort(key=lambda x: x[0])
    
    print(f"\nProcessing finished! {processed}/{total} processed, {real_found} real covers found.", flush=True)
    
    # Save SQL script database/fix_cover_urls.sql
    with open('database/fix_cover_urls.sql', 'w', encoding='utf-8') as f:
        f.write("-- SQL Script updating books table with real Open Library cover images\n")
        f.write("USE pagemind;\n\n")
        for book_id, title, cover_url in results:
            escaped_title = title.replace("'", "''")
            f.write(f"UPDATE books SET cover_url = '{cover_url}' WHERE id = {book_id}; -- {escaped_title}\n")
            
    print("Saved database/fix_cover_urls.sql successfully.", flush=True)

if __name__ == '__main__':
    main()
