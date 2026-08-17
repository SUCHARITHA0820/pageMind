from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class BookDetailsPage(BasePage):
    """Page Object for Book Details view screen."""

    # Locators
    BOOK_TITLE = (AppiumBy.XPATH, "//android.widget.TextView[@font-weight='bold' or @fontSize > 20]")
    BOOK_AUTHOR = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'By ') or contains(@text, 'Author')]")
    COVER_IMAGE = (AppiumBy.XPATH, "//android.widget.ImageView[contains(@content-desc, 'Cover')]")
    GRADIENT_PLACEHOLDER = (AppiumBy.XPATH, "//android.view.ViewGroup[contains(@content-desc, 'Cover Placeholder')]")
    BOOK_SUMMARY = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Summary') or contains(@text, 'Description')]")
    FAVORITE_TOGGLE_BTN = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Bookmark' or @content-desc='Favorite']")
    BACK_BTN = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Back']")

    # Actions
    def is_book_details_displayed(self):
        return self.is_displayed(*self.BOOK_TITLE) or self.is_displayed(*self.BOOK_SUMMARY)

    def toggle_favorite(self):
        self.click(*self.FAVORITE_TOGGLE_BTN)

    def go_back(self):
        self.click(*self.BACK_BTN)
