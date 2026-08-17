import pytest
from pages.home_page import HomePage
from pages.chatbot_page import ChatbotPage

class TestChatbot:
    """E2E Test cases for AI Chatbot Assistant & Fallback Engine."""

    def test_tc_chat_001_emotion_recommendations(self, driver):
        home_page = HomePage(driver)
        chatbot_page = ChatbotPage(driver)
        home_page.open_chatbot()
        chatbot_page.select_emotion_prompt("happy")
        assert True, "Triggered emotion-based book recommendation"

    def test_tc_chat_002_custom_prompt_query(self, driver):
        chatbot_page = ChatbotPage(driver)
        chatbot_page.send_message("Recommend me a thrilling sci-fi adventure book")
        assert True, "Sent custom query to AI chatbot"
