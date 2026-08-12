import re
import logging
from typing import Dict, Any, List, Optional

try:
    from langchain_ollama import ChatOllama
except ImportError:
    ChatOllama = None

logger = logging.getLogger("ai_agent_4node")

FOLLOWUP_KEYWORDS = [
    "oldest", "newest", "earliest", "most recent", "published first", "publication year",
    "best rated", "highest rated", "top rated", "best rating", "highest rating", "rating", "best", "top",
    "shortest description", "longest description", "brief description",
    "tell me more", "details", "detail", "summary of", "explain", "more about",
    "second one", "first one", "third one", "fourth one", "fifth one",
    "book 1", "book 2", "book 3", "book 4", "book 5",
    "#1", "#2", "#3", "#4", "#5",
    "which one is", "which is"
]

def classify_intent(message: str, stored_books: List[Dict[str, Any]]) -> str:
    """Classifies whether message is a FOLLOW_UP or NEW_REQUEST."""
    if not stored_books:
        return "NEW_REQUEST"

    text = message.strip()
    lower_text = text.lower()

    # 1. Try Ollama LLM if available
    if ChatOllama is not None:
        try:
            llm = ChatOllama(model="llama3.2:1b", timeout=3)
            prompt = (
                f"The user previously received book recommendations: {[b.get('title') for b in stored_books]}.\n"
                f"Classify their new message into EXACTLY one of these two options: 'FOLLOW_UP' or 'NEW_REQUEST'.\n"
                f"- Return 'FOLLOW_UP' if the user is asking to sort, filter, ask details about, or refine the existing recommendations (e.g. asking for oldest, newest, best rated, shortest description, or more info on a specific book).\n"
                f"- Return 'NEW_REQUEST' if the user is describing a new mood, asking for a different genre, or starting a new recommendation request.\n\n"
                f"User Message: '{text}'\n"
                f"Classification (FOLLOW_UP or NEW_REQUEST):"
            )
            response = llm.invoke(prompt)
            content = str(response.content if hasattr(response, 'content') else response).strip().upper()
            if "FOLLOW_UP" in content or "FOLLOWUP" in content:
                print(f"[followup_handler] Ollama classified intent as: 'FOLLOW_UP'", flush=True)
                return "FOLLOW_UP"
            elif "NEW_REQUEST" in content or "NEW" in content:
                print(f"[followup_handler] Ollama classified intent as: 'NEW_REQUEST'", flush=True)
                return "NEW_REQUEST"
        except Exception as e:
            print(f"[followup_handler] Ollama classification unavailable/timed out ({e}), falling back to keyword heuristics.", flush=True)

    # 2. Heuristic keyword fallback
    for kw in FOLLOWUP_KEYWORDS:
        if kw in lower_text:
            print(f"[followup_handler] Keyword heuristic matched '{kw}' -> classified as 'FOLLOW_UP'", flush=True)
            return "FOLLOW_UP"

    print(f"[followup_handler] Heuristic classified as 'NEW_REQUEST'", flush=True)
    return "NEW_REQUEST"

def followup_handler_node(state: Dict[str, Any]) -> Dict[str, Any]:
    user_msg = state.get("message", "")
    stored_books = state.get("stored_books", [])
    print(f"\n--- [Node: followup_handler] Processing Follow-up Message: '{user_msg}' ---", flush=True)
    logger.info(f"[Node: followup_handler] Processing follow-up message: '{user_msg}' over {len(stored_books)} stored books.")

    if not stored_books:
        print("[Node: followup_handler] No stored books found, returning unchanged state.", flush=True)
        return state

    lower_msg = user_msg.lower()
    filtered_books = list(stored_books)
    summary_action = "Refined previous recommendations."

    # A. Check for "oldest" / "earliest" / "published first"
    if any(w in lower_msg for w in ["oldest", "earliest", "published first", "first published"]):
        valid_years = [b.get("published_year") for b in stored_books if b.get("published_year") is not None]
        if valid_years:
            min_year = min(valid_years)
            filtered_books = [b for b in stored_books if b.get("published_year") == min_year]
            if len(filtered_books) == 1:
                summary_action = f"Here's the oldest book from your recommendations (published in {min_year}):"
            else:
                summary_action = f"These books are tied for the oldest from your recommendations (published in {min_year}):"
        print(f"[Node: followup_handler] Action: Filter min published_year -> {len(filtered_books)} book(s)", flush=True)

    # B. Check for "newest" / "most recent" / "latest"
    elif any(w in lower_msg for w in ["newest", "most recent", "latest", "recent"]):
        valid_years = [b.get("published_year") for b in stored_books if b.get("published_year") is not None]
        if valid_years:
            max_year = max(valid_years)
            filtered_books = [b for b in stored_books if b.get("published_year") == max_year]
            if len(filtered_books) == 1:
                summary_action = f"Here's the newest book from your recommendations (published in {max_year}):"
            else:
                summary_action = f"These books are tied for the newest from your recommendations (published in {max_year}):"
        print(f"[Node: followup_handler] Action: Filter max published_year -> {len(filtered_books)} book(s)", flush=True)

    # C. Check for "best rated" / "highest rated" / "top rated" / "best" / "top" / "rating"
    elif any(w in lower_msg for w in ["best rated", "highest rated", "top rated", "best rating", "highest rating", "rating", "best", "top"]):
        valid_ratings = [float(b.get("rating")) for b in stored_books if b.get("rating") is not None]
        if valid_ratings:
            max_rating = max(valid_ratings)
            filtered_books = [b for b in stored_books if b.get("rating") is not None and float(b.get("rating")) == max_rating]
            if len(filtered_books) == 1:
                summary_action = f"Here's the top-rated book from your recommendations (rated {max_rating:.1f}):"
            else:
                summary_action = f"These books are tied for the top-rated from your recommendations (rated {max_rating:.1f}):"
        print(f"[Node: followup_handler] Action: Filter max rating -> {len(filtered_books)} book(s)", flush=True)

    # D. Check for "shortest description" / "brief description"
    elif any(w in lower_msg for w in ["shortest description", "brief description", "short description"]):
        min_len = min(len(str(b.get("description", ""))) for b in stored_books)
        filtered_books = [b for b in stored_books if len(str(b.get("description", ""))) == min_len]
        if len(filtered_books) == 1:
            summary_action = "Here's the book with the shortest description from your recommendations:"
        else:
            summary_action = "These books are tied for the shortest description from your recommendations:"
        print(f"[Node: followup_handler] Action: Filter min description length -> {len(filtered_books)} book(s)", flush=True)

    # E. Check for "longest description"
    elif "longest description" in lower_msg:
        max_len = max(len(str(b.get("description", ""))) for b in stored_books)
        filtered_books = [b for b in stored_books if len(str(b.get("description", ""))) == max_len]
        if len(filtered_books) == 1:
            summary_action = "Here's the book with the longest description from your recommendations:"
        else:
            summary_action = "These books are tied for the longest description from your recommendations:"
        print(f"[Node: followup_handler] Action: Filter max description length -> {len(filtered_books)} book(s)", flush=True)

    # F. Check for specific book index/reference ("second one", "tell me more about #2", etc.)
    elif any(phrase in lower_msg for phrase in ["tell me more", "more details", "details on", "about the", "second", "first", "third", "fourth", "fifth", "book 1", "book 2", "book 3", "book 4", "book 5", "#1", "#2", "#3", "#4", "#5"]):
        target_idx = None
        if "first" in lower_msg or "1st" in lower_msg or "book 1" in lower_msg or "#1" in lower_msg:
            target_idx = 0
        elif "second" in lower_msg or "2nd" in lower_msg or "book 2" in lower_msg or "#2" in lower_msg:
            target_idx = 1
        elif "third" in lower_msg or "3rd" in lower_msg or "book 3" in lower_msg or "#3" in lower_msg:
            target_idx = 2
        elif "fourth" in lower_msg or "4th" in lower_msg or "book 4" in lower_msg or "#4" in lower_msg:
            target_idx = 3
        elif "fifth" in lower_msg or "5th" in lower_msg or "book 5" in lower_msg or "#5" in lower_msg:
            target_idx = 4
        
        if target_idx is not None and 0 <= target_idx < len(stored_books):
            target_book = stored_books[target_idx]
            filtered_books = [target_book]
            summary_action = f"Details for '{target_book.get('title')}': {target_book.get('description', '')}"
            print(f"[Node: followup_handler] Action: Expanded details for book index {target_idx} ('{target_book.get('title')}')", flush=True)

    # State updates
    state["books"] = filtered_books
    state["followup_summary"] = summary_action
    logger.info(f"[Node: followup_handler OUTPUT] {summary_action} Returned {len(filtered_books)} books.")
    return state
