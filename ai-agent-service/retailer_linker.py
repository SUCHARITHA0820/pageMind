import urllib.parse
import logging
from typing import Dict, Any, List

logger = logging.getLogger("ai_agent_4node")

def generate_retailer_links(books: List[Dict[str, Any]]) -> List[Dict[str, str]]:
    logger.info(f"[Node 4: retailer_linker] Generating retailer links for {len(books)} books")
    links = []
    for book in books:
        title = book.get("title", "")
        author = book.get("author", "")
        query = f"{title} {author}".strip()
        encoded_query = urllib.parse.quote(query)

        amazon_url = f"https://www.amazon.in/s?k={encoded_query}"
        flipkart_url = f"https://www.flipkart.com/search?q={encoded_query}"

        links.append({
            "title": title,
            "amazon": amazon_url,
            "flipkart": flipkart_url
        })
    logger.info(f"[Node 4: retailer_linker] Generated {len(links)} retailer link pairs.")
    return links

def retailer_linker_node(state: Dict[str, Any]) -> Dict[str, Any]:
    books = state.get("books", [])
    links = generate_retailer_links(books)
    state["retailer_links"] = links
    logger.info(f"[Node 4: retailer_linker OUTPUT] Retailer Links Generated: {len(links)}")
    return state
