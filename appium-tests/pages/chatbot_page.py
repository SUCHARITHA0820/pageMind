from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class ChatbotPage(BasePage):
    """Page Object for AI Chatbot Assistant & Recommendation Screen."""

    # Locators
    CHAT_INPUT = (AppiumBy.XPATH, "//android.widget.EditText[contains(@text, 'Ask PageMind') or contains(@hint, 'Type')]")
    SEND_BTN = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Send message']")
    EMOTION_HAPPY = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Happy') or contains(@text, 'Joy')]")
    EMOTION_ANXIOUS = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Anxious') or contains(@text, 'Calm')]")
    EMOTION_ADVENTURE = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Adventure') or contains(@text, 'Thrill')]")
    RECOMMENDED_BOOK_CARD = (AppiumBy.XPATH, "(//android.widget.TextView[contains(@text, 'Author') or contains(@text, 'by')])[1]")
    BUY_AMAZON_BTN = (AppiumBy.XPATH, "(//android.widget.Button[contains(@text, 'Amazon')])[1]")
    BUY_FLIPKART_BTN = (AppiumBy.XPATH, "(//android.widget.Button[contains(@text, 'Flipkart')])[1]")

    # Actions
    def send_message(self, text):
        self.type_text(*self.CHAT_INPUT, text)
        self.click(*self.SEND_BTN)

    def select_emotion_prompt(self, emotion="happy"):
        if emotion.lower() == "anxious":
            self.click(*self.EMOTION_ANXIOUS)
        elif emotion.lower() == "adventure":
            self.click(*self.EMOTION_ADVENTURE)
        else:
            self.click(*self.EMOTION_HAPPY)

    def click_buy_on_amazon(self):
        self.click(*self.BUY_AMAZON_BTN)
