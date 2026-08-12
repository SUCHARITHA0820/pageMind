from typing import Optional, List, Dict, Any
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn
from agent import run_agent

app = FastAPI(
    title="PageMind Conversational LangGraph AI Agent Service",
    description="Multi-step LangGraph Agent featuring mood_analyzer, genre_mapper, book_retriever, followup_handler, and retailer_linker",
    version="2.1.0"
)

class RecommendRequest(BaseModel):
    message: Optional[str] = None
    prompt: Optional[str] = None
    userId: Optional[int] = None
    user_id: Optional[int] = None
    sessionId: Optional[str] = None
    session_id: Optional[str] = None

class RecommendResponse(BaseModel):
    mood: str
    genre: str
    books: List[Dict[str, Any]]
    retailer_links: List[Dict[str, Any]]
    session_id: Optional[str] = None
    is_followup: Optional[bool] = False
    # Backwards compatibility / alias fields for backend integration
    detected_genre: Optional[str] = None
    recommended_books: Optional[List[Dict[str, Any]]] = None
    message: Optional[str] = None

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "ai-agent-service-conversational"}

@app.post("/recommend", response_model=RecommendResponse)
def recommend(request: RecommendRequest):
    user_message = request.message or request.prompt
    if not user_message or not user_message.strip():
        user_message = "I am feeling curious and want a good book recommendation"

    effective_user_id = request.userId if request.userId is not None else request.user_id
    effective_session_id = request.sessionId or request.session_id or (str(effective_user_id) if effective_user_id is not None else None)

    try:
        result = run_agent(message=user_message, user_id=effective_user_id, session_id=effective_session_id)

        mood = result.get("mood", "curious")
        genre = result.get("genre", "General Fiction")
        books = result.get("books", [])
        retailer_links = result.get("retailer_links", [])
        is_followup = result.get("is_followup", False)
        followup_summary = result.get("followup_summary")

        if is_followup and followup_summary:
            summary_msg = followup_summary
        elif is_followup:
            summary_msg = f"Refined recommendations for mood '{mood}'. Returned {len(books)} books."
        else:
            summary_msg = f"Detected mood '{mood}' mapped to {genre}. Found {len(books)} recommendations."

        return RecommendResponse(
            mood=mood,
            genre=genre,
            books=books,
            retailer_links=retailer_links,
            session_id=effective_session_id,
            is_followup=is_followup,
            detected_genre=genre,
            recommended_books=books,
            message=summary_msg
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI Agent graph execution error: {str(e)}")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
