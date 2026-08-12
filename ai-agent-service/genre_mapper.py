import logging
from typing import Dict, Any, List

logger = logging.getLogger("ai_agent_4node")

MOOD_GENRE_MAP: Dict[str, List[str]] = {
    "sad": ["Romance", "Self-Help"],
    "heartbroken": ["Romance", "Classic Literature"],
    "anxious": ["Self-Help", "Poetry"],
    "stressed": ["Self-Help", "Poetry"],
    "happy": ["Fantasy", "Science Fiction"],
    "excited": ["Fantasy", "Young Adult"],
    "adventurous": ["Fantasy", "Science Fiction"],
    "bored": ["Mystery", "Thriller"],
    "angry": ["Thriller", "Fantasy"],
    "nostalgic": ["Classic Literature", "History"],
    "romantic": ["Romance", "Poetry"],
    "scared": ["Horror", "Thriller"],
    "gloomy": ["Horror", "Mystery"],
    "curious": ["Non-Fiction", "Philosophy"],
    "lonely": ["Fiction", "Romance"],
    "inspired": ["Self-Help", "Biography"],
    "relaxed": ["Poetry", "Fiction"]
}

def map_mood_to_genres(mood: str) -> List[str]:
    clean_mood = mood.lower().strip() if mood else "curious"
    mapped_genres = MOOD_GENRE_MAP.get(clean_mood, ["Fiction", "Non-Fiction"])
    print(f"--- [Node 2: genre_mapper] Received Mood: '{clean_mood}' ---> Mapped Genres: {mapped_genres}", flush=True)
    return mapped_genres

def genre_mapper_node(state: Dict[str, Any]) -> Dict[str, Any]:
    mood = state.get("mood", "curious")
    print(f"\n[Node 2: genre_mapper] Mood Received: '{mood}'", flush=True)
    logger.info(f"[Node 2: genre_mapper] Mood Received: '{mood}'")
    
    genres = map_mood_to_genres(mood)
    state["genres"] = genres
    state["genre"] = ", ".join(genres)
    
    print(f"[Node 2: genre_mapper] Mapped Genre(s): {genres}", flush=True)
    logger.info(f"[Node 2: genre_mapper] Mapped Genre(s): {genres}")
    return state
