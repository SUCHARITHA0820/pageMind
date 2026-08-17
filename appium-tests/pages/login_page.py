from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class LoginPage(BasePage):
    """Page Object for Authentication (Login / Signup / Password Reset) screens."""

    # Locators
    TITLE_TEXT = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'PageMind')]")
    EMAIL_INPUT = (AppiumBy.XPATH, "//android.widget.EditText[contains(@text, 'Email') or contains(@hint, 'Email')]")
    PASSWORD_INPUT = (AppiumBy.XPATH, "//android.widget.EditText[contains(@text, 'Password') or contains(@hint, 'Password')]")
    LOGIN_BUTTON = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'Sign In') or contains(@text, 'Login')]")
    SIGNUP_LINK = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Sign Up') or contains(@text, 'Create Account')]")
    FORGOT_PASSWORD_LINK = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Forgot Password')]")
    GOOGLE_AUTH_BTN = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'Google')]")
    GITHUB_AUTH_BTN = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'GitHub')]")
    ERROR_MSG = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'Invalid') or contains(@text, 'Error')]")

    # Actions
    def login(self, email, password):
        self.type_text(*self.EMAIL_INPUT, email)
        self.type_text(*self.PASSWORD_INPUT, password)
        self.click(*self.LOGIN_BUTTON)

    def is_login_screen_displayed(self):
        return self.is_displayed(*self.LOGIN_BUTTON)

    def navigate_to_signup(self):
        self.click(*self.SIGNUP_LINK)

    def navigate_to_forgot_password(self):
        self.click(*self.FORGOT_PASSWORD_LINK)

    def get_error_message(self):
        return self.get_text(*self.ERROR_MSG)
