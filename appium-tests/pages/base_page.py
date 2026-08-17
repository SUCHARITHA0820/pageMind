import time
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from appium.webdriver.common.appiumby import AppiumBy
from config import EXPLICIT_WAIT_TIMEOUT

class BasePage:
    """Base class for all Page Object models providing wrapper methods."""

    def __init__(self, driver):
        self.driver = driver
        self.wait = WebDriverWait(driver, EXPLICIT_WAIT_TIMEOUT)

    def find_element(self, by, locator):
        return self.wait.until(EC.presence_of_element_located((by, locator)))

    def find_visible_element(self, by, locator):
        return self.wait.until(EC.visibility_of_element_located((by, locator)))

    def click(self, by, locator):
        element = self.find_visible_element(by, locator)
        element.click()

    def type_text(self, by, locator, text, clear=True):
        element = self.find_visible_element(by, locator)
        if clear:
            element.clear()
        element.send_keys(text)

    def get_text(self, by, locator):
        element = self.find_visible_element(by, locator)
        return element.text

    def is_displayed(self, by, locator):
        try:
            return self.find_visible_element(by, locator).is_displayed()
        except Exception:
            return False

    def scroll_down(self):
        """Scrolls down on the mobile screen."""
        size = self.driver.get_window_size()
        start_x = size['width'] // 2
        start_y = int(size['height'] * 0.8)
        end_y = int(size['height'] * 0.2)
        self.driver.swipe(start_x, start_y, start_x, end_y, 800)

    def scroll_to_element(self, text):
        """Scrolls until element with exact text is visible using UiScrollable."""
        ui_automator_str = (
            'new UiScrollable(new UiSelector().scrollable(true).instance(0))'
             f'.scrollIntoView(new UiSelector().textContains("{text}").instance(0))'
        )
        return self.driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR, ui_automator_str)
