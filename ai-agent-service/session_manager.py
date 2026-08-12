import logging
import time
from typing import Dict, Any, List, Optional

logger = logging.getLogger("ai_agent_4node")

# Global in-memory session store mapping session_id -> session state
SESSION_STORE: Dict[str, Dict[str, Any]] = {}

def get_session(session_id: Optional[str]) -> Optional[Dict[str, Any]]:
    if not session_id:
        return None
    session_key = str(session_id).strip()
    session = SESSION_STORE.get(session_key)
    if session:
        logger.info(f"[SessionManager] Retrieved session '{session_key}' with {len(session.get('books', []))} cached books.")
    else:
        logger.info(f"[SessionManager] No existing session found for key '{session_key}'.")
    return session

def save_session(session_id: Optional[str], books: List[Dict[str, Any]], mood: Optional[str] = None, genre: Optional[str] = None) -> None:
    if not session_id:
        return
    session_key = str(session_id).strip()
    SESSION_STORE[session_key] = {
        "books": books,
        "mood": mood,
        "genre": genre,
        "updated_at": time.time()
    }
    logger.info(f"[SessionManager] Saved session '{session_key}' with {len(books)} books (Mood: {mood}, Genre: {genre}).")

def clear_session(session_id: Optional[str]) -> None:
    if not session_id:
        return
    session_key = str(session_id).strip()
    if session_key in SESSION_STORE:
        del SESSION_STORE[session_key]
        logger.info(f"[SessionManager] Cleared session '{session_key}'.")
