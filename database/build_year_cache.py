import json
import os
import re
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
import glob
import importlib.util

CACHE_FILE = r"c:\pdd1\database\book_years_cache.json"

KNOWN_YEARS = {
    "To Kill a Mockingbird": 1960,
    "The Great Gatsby": 1925,
    "1984": 1949,
    "Pride and Prejudice": 1813,
    "The Catcher in the Rye": 1951,
    "One Hundred Years of Solitude": 1967,
    "Brave New World": 1932,
    "The Alchemist": 1988,
    "The Kite Runner": 2003,
    "Life of Pi": 2001,
    "Beloved": 1987,
    "The Book Thief": 2005,
    "Fahrenheit 451": 1953,
    "The Road": 2006,
    "Slaughterhouse-Five": 1969,
    "Jane Eyre": 1847,
    "Wuthering Heights": 1847,
    "Lord of the Flies": 1954,
    "Animal Farm": 1945,
    "The Grapes of Wrath": 1939,
    "Of Mice and Men": 1937,
    "Catch-22": 1961,
    "The Color Purple": 1982,
    "The God of Small Things": 1997,
    "Midnight's Children": 1981,
    "A Thousand Splendid Suns": 2007,
    "Where the Crawdads Sing": 2018,
    "Normal People": 2018,
    "The Midnight Library": 2020,
    "A Man Called Ove": 2012,
    "The Shadow of the Wind": 2001,
    "All the Light We Cannot See": 2014,
    "Little Fires Everywhere": 2017,
    "Room": 2010,
    "The Sympathizer": 2015,
    "Station Eleven": 2014,
    "Cloud Atlas": 2004,
    "The Seven Deaths of Evelyn Hardcastle": 2018,
    "Project Hail Mary": 2021,
    "The Martian": 2011,
    "Frankenstein": 1818,
    "Dracula": 1897,
    "Moby-Dick": 1851,
    "Crime and Punishment": 1866,
    "The Picture of Dorian Gray": 1890,
    "War and Peace": 1869,
    "The Hobbit": 1937,
    "The Fellowship of the Ring": 1954,
    "Dune": 1965,
    "Foundation": 1951,
    "Neuromancer": 1984,
    "Snow Crash": 1992,
    "Ender's Game": 1985,
    "The Left Hand of Darkness": 1969,
    "Hyperion": 1989,
    "Do Androids Dream of Electric Sheep?": 1968,
    "The Hitchhiker's Guide to the Galaxy": 1979,
    "A Game of Thrones": 1996,
    "The Name of the Wind": 2007,
    "The Way of Kings": 2010,
    "Harry Potter and the Sorcerer's Stone": 1997,
    "The Hunger Games": 2008,
    "Thinking, Fast and Slow": 2011,
    "Atomic Habits": 2018,
    "Sapiens: A Brief History of Humankind": 2011,
    "Educated": 2018,
    "Becoming": 2018,
    "Born a Crime": 2016,
    "When Breath Becomes Air": 2016,
    "Quiet: The Power of Introverts in a World That Can't Stop Talking": 2012,
    "Man's Search for Meaning": 1946,
    "The Power of Habit": 2012,
    "Guns, Germs, and Steel": 1997,
    "Cosmos": 1980,
    "A Short History of Nearly Everything": 2003,
    "The Selfish Gene": 1976,
    "Outliers: The Story of Success": 2008,
    "Freakonomics": 2005,
    "Clean Code": 2008,
    "Design Patterns": 1994,
    "The Pragmatic Programmer": 1999,
    "The Art of War": -500, # or 1910
    "Meditations": 180,
    "The Republic": -375
}

def load_cache():
    if os.path.exists(CACHE_FILE):
        try:
            with open(CACHE_FILE, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception:
            pass
    return {}

def save_cache(cache):
    with open(CACHE_FILE, 'w', encoding='utf-8') as f:
        json.dump(cache, f, indent=2)

def fetch_openlibrary_year(isbn):
    if not isbn or len(isbn) < 9:
        return None
    url = f"https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'PageMindSeedFetcher/1.0'})
        with urllib.request.urlopen(req, timeout=3) as resp:
            data = json.loads(resp.read().decode())
            key = f"ISBN:{isbn}"
            if key in data and 'publish_date' in data[key]:
                pdate = data[key]['publish_date']
                m = re.search(r'\b(1[789]\d{2}|20[0-2]\d)\b', str(pdate))
                if m:
                    return int(m.group(1))
    except Exception:
        pass
    return None

def main():
    cache = load_cache()
    generators = sorted(glob.glob(r"c:\pdd1\database\generate_*.py"))
    
    all_books = []
    for gen in generators:
        spec = importlib.util.spec_from_file_location("module", gen)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        
        data_dict = None
        for name in ['GENRE_DATA', 'GENRE_DATA_BATCH_2', 'GENRE_DATA_BATCH_3', 'GENRE_DATA_BATCH_4', 
                     'GENRE_DATA_BATCH_5', 'GENRE_DATA_BATCH_6', 'GENRE_DATA_BATCH_7', 'GENRE_DATA_BATCH_8',
                     'GENRE_DATA_BATCH_9', 'genres_data']:
            if hasattr(mod, name):
                data_dict = getattr(mod, name)
                break
        
        if data_dict:
            for genre, books in data_dict.items():
                for item in books:
                    title, author, isbn, desc = item[0], item[1], item[2], item[3]
                    all_books.append((title, author, isbn, genre, desc))

    print(f"Collected {len(all_books)} books total.")

    to_fetch = []
    resolved = 0
    for title, author, isbn, genre, desc in all_books:
        book_key = f"{title}||{author}"
        if book_key in cache:
            resolved += 1
            continue
        if title in KNOWN_YEARS:
            year = KNOWN_YEARS[title]
            cache[book_key] = year if year > 0 else 1900
            resolved += 1
            continue
        if isbn:
            to_fetch.append((book_key, isbn))

    print(f"Already cached/known: {resolved}. Needs API fetch: {len(to_fetch)}")

    if to_fetch:
        print(f"Fetching publication years from OpenLibrary for {len(to_fetch)} ISBNs...")
        fetched_count = 0
        with ThreadPoolExecutor(max_workers=20) as executor:
            future_to_key = {executor.submit(fetch_openlibrary_year, isbn): key for key, isbn in to_fetch}
            for future in as_completed(future_to_key):
                key = future_to_key[future]
                try:
                    res = future.result()
                    if res:
                        cache[key] = res
                        fetched_count += 1
                except Exception:
                    pass
        print(f"Successfully fetched {fetched_count} years from API.")

    save_cache(cache)
    print(f"Total entries in cache now: {len(cache)}")

if __name__ == "__main__":
    main()
