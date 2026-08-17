from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class HomePage(BasePage):
    """Page Object for Home Dashboard & Book Catalog Feed."""

    # Locators
    GREETING_TEXT = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Welcome') or contains(@text, 'PageMind')]")
    SEARCH_BAR_ICON = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Search']")
    CATEGORY_ALL = (AppiumBy.XPATH, "//android.widget.TextView[@text='All']")
    CATEGORY_FICTION = (AppiumBy.XPATH, "//android.widget.TextView[@text='Fiction']")
    CATEGORY_SELF_HELP = (AppiumBy.XPATH, "//android.widget.TextView[@text='Self-Help']")
    BOOK_CARD_FIRST = (AppiumBy.XPATH, "(//android.view.ViewGroup[contains(@content-desc, 'Book')])[1]")
    LIKE_BUTTON_FIRST = (AppiumBy.XPATH, "(//android.widget.ImageView[contains(@content-desc, 'Like') or contains(@content-desc, 'Favorite')])[1]")
    NAV_HOME = (AppiumBy.XPATH, "//android.widget.TextView[@text='Home']")
    NAV_SEARCH = (AppiumBy.XPATH, "//android.widget.TextView[@text='Search']")
    NAV_CHATBOT = (AppiumBy.XPATH, "//android.widget.TextView[@text='AI Assistant']")
    NAV_PROFILE = (AppiumBy.XPATH, "//android.widget.TextView[@text='Profile']")

    # Actions
    def is_home_loaded(self):
        return self.is_displayed(*self.GREETING_TEXT)

    def click_category_pill(self, category_name):
        locator = (AppiumBy.XPATH, f"//android.widget.TextView[@text='{category_name}']")
        self.click(*locator)

    def select_first_book(self):
        self.click(*self.BOOK_CARD_FIRST)

    def open_chatbot(self):
        self.click(*self.NAV_CHATBOT)

    def open_profile(self):
        self.click(*self.NAV_PROFILE)

    def open_search(self):
        self.click(*self.NAV_SEARCH)
