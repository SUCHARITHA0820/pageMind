import logging
from typing import TypedDict, Optional, List, Dict, Any
from langgraph.graph import StateGraph, END

from mood_analyzer import mood_analyzer_node
from genre_mapper import genre_mapper_node
from book_retriever import book_retriever_node
from retailer_linker import retailer_linker_node
from followup_handler import followup_handler_node, classify_intent
from session_manager import get_session, save_session

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ai_agent_4node")

class AgentState(TypedDict):
    message: str
    user_id: Optional[int]
    session_id: Optional[str]
    mood: Optional[str]
    genres: Optional[List[str]]
    genre: Optional[str]
    books: List[Dict[str, Any]]
    retailer_links: List[Dict[str, str]]
    stored_books: Optional[List[Dict[str, Any]]]
    is_followup: Optional[bool]
    followup_summary: Optional[str]

def route_intent(state: AgentState) -> str:
    if state.get("is_followup") and state.get("stored_books"):
        logger.info("[AgentGraph Router] Routing to 'followup_handler'")
        return "followup_handler"
    else:
        logger.info("[AgentGraph Router] Routing to 'mood_analyzer'")
        return "mood_analyzer"

def create_agent_graph():
    builder = StateGraph(AgentState)
    builder.add_node("mood_analyzer", mood_analyzer_node)
    builder.add_node("genre_mapper", genre_mapper_node)
    builder.add_node("book_retriever", book_retriever_node)
    builder.add_node("followup_handler", followup_handler_node)
    builder.add_node("retailer_linker", retailer_linker_node)

    builder.set_conditional_entry_point(
        route_intent,
        {
            "followup_handler": "followup_handler",
            "mood_analyzer": "mood_analyzer"
        }
    )

    builder.add_edge("mood_analyzer", "genre_mapper")
    builder.add_edge("genre_mapper", "book_retriever")
    builder.add_edge("book_retriever", "retailer_linker")
    builder.add_edge("followup_handler", "retailer_linker")
    builder.add_edge("retailer_linker", END)

    return builder.compile()

agent_graph = create_agent_graph()

def run_agent(message: str, user_id: Optional[int] = None, session_id: Optional[str] = None) -> Dict[str, Any]:
    logger.info(f"=== Starting LangGraph Conversational Agent Run ===")
    
    # Check session memory if session_id (or user_id fallback) is provided
    eff_session_id = session_id or (str(user_id) if user_id is not None else None)
    session_data = get_session(eff_session_id)
    
    stored_books = session_data.get("books", []) if session_data else []
    cached_mood = session_data.get("mood") if session_data else None
    cached_genre = session_data.get("genre") if session_data else None

    is_followup = False
    if stored_books:
        intent = classify_intent(message, stored_books)
        if intent == "FOLLOW_UP":
            is_followup = True
            logger.info(f"Detected FOLLOW_UP request for session '{eff_session_id}' on {len(stored_books)} stored books.")
        else:
            logger.info(f"Detected NEW_REQUEST for session '{eff_session_id}'. Re-running full recommendation pipeline.")

    initial_state: AgentState = {
        "message": message,
        "user_id": user_id,
        "session_id": eff_session_id,
        "mood": cached_mood if is_followup else None,
        "genres": [g.strip() for g in cached_genre.split(",")] if (is_followup and cached_genre) else [],
        "genre": cached_genre if is_followup else None,
        "books": [],
        "retailer_links": [],
        "stored_books": stored_books if is_followup else [],
        "is_followup": is_followup,
        "followup_summary": None
    }

    final_state = agent_graph.invoke(initial_state)

    final_books = final_state.get("books", [])
    final_mood = final_state.get("mood", "curious")
    final_genre = final_state.get("genre", "Fiction")

    # Update session memory
    if eff_session_id and final_books:
        if not is_followup:
            save_session(eff_session_id, final_books, mood=final_mood, genre=final_genre)
        else:
            save_session(eff_session_id, stored_books, mood=final_mood, genre=final_genre)

    logger.info(f"=== Completed LangGraph Agent Run (is_followup={is_followup}, books={len(final_books)}) ===")

    return {
        "mood": final_mood,
        "genre": final_genre,
        "genres": final_state.get("genres", [final_genre]),
        "books": final_books,
        "retailer_links": final_state.get("retailer_links", []),
        "is_followup": is_followup,
        "followup_summary": final_state.get("followup_summary")
    }
