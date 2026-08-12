import hashlib
import json
import os
import re

CACHE_FILE = os.path.join(os.path.dirname(__file__), "book_years_cache.json")
_cache = None

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
    "The Sense of an Ending": 2011,
    "Lincoln in the Bardo": 2017,
    "Piranesi": 2020,
    "A Fine Balance": 1995,
    "White Teeth": 2000,
    "Drive Your Plow Over the Bones of the Dead": 2009,
    "The Overstory": 2018,
    "Shuggie Bain": 2020,
    "Hamnet": 2020,
    "The Remains of the Day": 1989,
    "Never Let Me Go": 2005,
    "Klara and the Sun": 2021,
    "On Earth We're Briefly Gorgeous": 2019,
    "The Vanishing Half": 2020,
    "Homegoing": 2016,
    "Transcendent Kingdom": 2020,
    "Pachinko": 2017,
    "Free Food for Millionaires": 2007,
    "The Memory Police": 1994,
    "Convenience Store Woman": 2016,
    "Earthlings": 2018,
    "Before the Coffee Gets Cold": 2015,
    "The Seven Deaths of Evelyn Hardcastle": 2018,
    "The Moonstone": 1868,
    "The Woman in White": 1859,
    "In a Dark, Dark Wood": 2015,
    "The Woman in Cabin 10": 2016,
    "The Turn of the Key": 2019,
    "The Hunting Party": 2018,
    "The Paris Apartment": 2022,
    "Maisie Dobbs": 2003,
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
    "Clean Code": 2008,
    "Design Patterns": 1994,
    "The Pragmatic Programmer": 1999
}

GENRE_YEAR_RANGES = {
    "Classic Literature": (1800, 1950),
    "History": (1950, 2022),
    "Philosophy": (1850, 2020),
    "Poetry": (1850, 2020),
    "Science Fiction": (1950, 2023),
    "Fantasy": (1970, 2023),
    "Horror": (1970, 2023),
    "Mystery": (1975, 2023),
    "Thriller": (1975, 2023),
    "Romance": (1980, 2023),
    "Fiction": (1980, 2023),
    "Self-Help": (1995, 2023),
    "Young Adult": (1995, 2023),
    "Non-Fiction": (1990, 2023),
    "Biography": (1980, 2023)
}

def get_rating(title, author):
    """Returns a deterministic rating between 3.5 and 4.8 rounded to 1 decimal place."""
    h = int(hashlib.md5(f"{title}:{author}".encode('utf-8')).hexdigest(), 16)
    val = 3.5 + (h % 134) / 100.0  # range 3.50 to 4.83
    return round(min(val, 4.8), 1)

def get_published_year(title, author, genre, desc="", isbn=""):
    """Returns a realistic published year matching real publication year / era."""
    # 1. Known title dictionary lookup
    if title in KNOWN_YEARS:
        return KNOWN_YEARS[title]

    # 2. Check if year is mentioned in description (e.g. "published in 1994", "In 1896...")
    m = re.search(r'\b(1[789]\d{2}|20[0-2]\d)\b', desc)
    if m:
        yr = int(m.group(1))
        # Ensure year is reasonable
        if 1750 <= yr <= 2024:
            return yr

    # 3. Deterministic hash-based year within genre's realistic timeframe
    min_year, max_year = GENRE_YEAR_RANGES.get(genre, (1980, 2023))
    h = int(hashlib.md5(f"year:{title}:{author}".encode('utf-8')).hexdigest(), 16)
    year = min_year + (h % (max_year - min_year + 1))
    return year
