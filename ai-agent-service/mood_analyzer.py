import logging
from typing import Dict, Any, Optional

try:
    from langchain_ollama import ChatOllama
except ImportError:
    ChatOllama = None

logger = logging.getLogger("ai_agent_4node")

KNOWN_MOODS = [
    "sad", "anxious", "happy", "romantic", "adventurous",
    "bored", "angry", "nostalgic", "heartbroken", "stressed",
    "curious", "lonely", "gloomy", "inspired", "excited",
    "relaxed", "scared"
]

def analyze_mood_text(text: str) -> str:
    print(f"\n--- [Node 1: mood_analyzer] Raw Input Message: '{text}' ---", flush=True)
    logger.info(f"[Node 1: mood_analyzer] Analyzing user message: '{text}'")
    detected_mood = None

    # 1. Try Ollama LLM if available
    if ChatOllama is not None:
        try:
            llm = ChatOllama(model="llama3.2:1b", timeout=3)
            prompt = (
                f"Classify the primary user mood from the text into exactly ONE of these words: "
                f"[{', '.join(KNOWN_MOODS)}]. "
                f"Return ONLY the single mood word, lowercased, nothing else.\n\n"
                f"User text: '{text}'\nDetected Mood:"
            )
            response = llm.invoke(prompt)
            content = str(response.content if hasattr(response, 'content') else response).strip().lower()
            for known_mood in KNOWN_MOODS:
                if known_mood in content:
                    detected_mood = known_mood
                    print(f"[Node 1: mood_analyzer] Ollama classified mood as: '{detected_mood}'", flush=True)
                    break
        except Exception as e:
            print(f"[Node 1: mood_analyzer] Ollama LLM unavailable/timed out ({e}), using keyword heuristics.", flush=True)

    # 2. Fallback heuristic keyword matching
    if not detected_mood:
        lower_text = text.lower()
        if any(w in lower_text for w in ["heartbreak", "heartbroken", "breakup", "dumped", "crying"]):
            detected_mood = "heartbroken"
        elif any(w in lower_text for w in ["sad", "depressed", "down", "unhappy", "blue", "grief", "tear"]):
            detected_mood = "sad"
        elif any(w in lower_text for w in ["anxious", "worry", "worried", "panic", "nervous", "fear"]):
            detected_mood = "anxious"
        elif any(w in lower_text for w in ["stress", "stressed", "overwhelmed", "exhausted", "tired"]):
            detected_mood = "stressed"
        elif any(w in lower_text for w in ["happy", "joy", "cheerful", "great", "good", "awesome", "delighted"]):
            detected_mood = "happy"
        elif any(w in lower_text for w in ["excited", "thrilled", "ecstatic", "hyped"]):
            detected_mood = "excited"
        elif any(w in lower_text for w in ["adventure", "adventurous", "explore", "travel", "journey", "quest"]):
            detected_mood = "adventurous"
        elif any(w in lower_text for w in ["bored", "boring", "nothing to do", "dull"]):
            detected_mood = "bored"
        elif any(w in lower_text for w in ["angry", "mad", "furious", "annoyed", "frustrated"]):
            detected_mood = "angry"
        elif any(w in lower_text for w in ["nostalgic", "nostalgia", "past", "memories", "reminisce", "old days"]):
            detected_mood = "nostalgic"
        elif any(w in lower_text for w in ["romantic", "love", "affection", "crush", "dating"]):
            detected_mood = "romantic"
        elif any(w in lower_text for w in ["scared", "scary", "spooky", "creepy", "frightened", "horror", "thrill", "thrilling"]):
            detected_mood = "scared"
        elif any(w in lower_text for w in ["dark", "gloomy", "morbid", "shadow"]):
            detected_mood = "gloomy"
        elif any(w in lower_text for w in ["lonely", "alone", "isolated", "miss"]):
            detected_mood = "lonely"
        elif any(w in lower_text for w in ["inspire", "inspired", "growth", "goal", "motive", "aspire"]):
            detected_mood = "inspired"
        elif any(w in lower_text for w in ["relax", "relaxed", "chill", "peace", "calm", "cozy"]):
            detected_mood = "relaxed"
        elif any(w in lower_text for w in ["curious", "learn", "wonder", "why", "understand", "science"]):
            detected_mood = "curious"
        else:
            detected_mood = "curious"

        print(f"[Node 1: mood_analyzer] Keyword heuristic classified mood as: '{detected_mood}'", flush=True)

    return detected_mood

def mood_analyzer_node(state: Dict[str, Any]) -> Dict[str, Any]:
    user_msg = state.get("message", "")
    print(f"\n[Node 1: mood_analyzer] Raw Input Message: '{user_msg}'", flush=True)
    logger.info(f"[Node 1: mood_analyzer] Raw Input Message: '{user_msg}'")
    
    mood = analyze_mood_text(user_msg)
    state["mood"] = mood
    
    print(f"[Node 1: mood_analyzer] Detected Mood Classification: '{mood}'", flush=True)
    logger.info(f"[Node 1: mood_analyzer] Detected Mood Classification: '{mood}'")
    return state

