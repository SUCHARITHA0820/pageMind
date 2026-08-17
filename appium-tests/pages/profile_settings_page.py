from appium.webdriver.common.appiumby import AppiumBy
from pages.base_page import BasePage

class ProfileSettingsPage(BasePage):
    """Page Object for User Profile, Settings, Theme, and Notifications."""

    # Locators
    SETTINGS_ICON = (AppiumBy.XPATH, "//android.widget.ImageView[@content-desc='Settings']")
    USER_NAME_TEXT = (AppiumBy.XPATH, "//android.widget.TextView[contains(@text, 'User') or contains(@text, 'Profile')]")
    DARK_THEME_SWITCH = (AppiumBy.XPATH, "(//android.widget.Switch)[1]")
    NOTIFICATIONS_SWITCH = (AppiumBy.XPATH, "(//android.widget.Switch)[2]")
    AVATAR_IMAGE = (AppiumBy.XPATH, "//android.widget.ImageView[contains(@content-desc, 'Avatar')]")
    LOGOUT_BTN = (AppiumBy.XPATH, "//android.widget.Button[contains(@text, 'Logout') or contains(@text, 'Sign Out')]")

    # Actions
    def open_settings(self):
        self.click(*self.SETTINGS_ICON)

    def toggle_dark_theme(self):
        self.click(*self.DARK_THEME_SWITCH)

    def toggle_notifications(self):
        self.click(*self.NOTIFICATIONS_SWITCH)

    def logout(self):
        self.click(*self.LOGOUT_BTN)
