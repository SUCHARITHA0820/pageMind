from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class OnboardingPage(BasePage):
    """Page Object for Onboarding Carousel & Language Selection screens."""

    # Locators
    SKIP_BTN = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Skip')]")
    NEXT_BTN = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'Next')]")
    GET_STARTED_BTN = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'Get Started')]")
    LANG_ENGLISH = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'English')]")
    LANG_TELUGU = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Telugu')]")
    LANG_SPANISH = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Spanish')]")
    LANG_FRENCH = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'French')]")
    CONFIRM_LANG_BTN = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'Continue')]")

    # Actions
    def complete_onboarding(self):
        if self.is_displayed(*self.SKIP_BTN):
            self.click(*self.SKIP_BTN)
        elif self.is_displayed(*self.GET_STARTED_BTN):
            self.click(*self.GET_STARTED_BTN)

    def select_language(self, lang_name="English"):
        if lang_name.lower() == "telugu":
            self.click(*self.LANG_TELUGU)
        elif lang_name.lower() == "spanish":
            self.click(*self.LANG_SPANISH)
        elif lang_name.lower() == "french":
            self.click(*self.LANG_FRENCH)
        else:
            self.click(*self.LANG_ENGLISH)
        
        if self.is_displayed(*self.CONFIRM_LANG_BTN):
            self.click(*self.CONFIRM_LANG_BTN)
