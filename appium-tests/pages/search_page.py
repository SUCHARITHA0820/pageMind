from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class SearchPage(BasePage):
    """Page Object for Book Search & Filtering screen."""

    # Locators
    SEARCH_INPUT = (AppiumBy.XPATH, "//android.widget.EditText[contains(@text, 'Search') or contains(@hint, 'Search')]")
    CLEAR_SEARCH_BTN = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Clear search']")
    RESULT_COUNT_HEADER = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Books') or contains(@text, 'results')]")
    NO_RESULTS_TEXT = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'No books found')]")
    NEXT_PAGE_BTN = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Next Page']")
    PREV_PAGE_BTN = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Previous Page']")

    # Actions
    def search_book(self, query):
        self.type_text(*self.SEARCH_INPUT, query)

    def clear_search(self):
        if self.is_displayed(*self.CLEAR_SEARCH_BTN):
            self.click(*self.CLEAR_SEARCH_BTN)

    def go_to_next_page(self):
        self.click(*self.NEXT_PAGE_BTN)
