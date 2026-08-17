import os
import time
import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options
from config import APPIUM_SERVER_URL, DESIRED_CAPABILITIES, IMPLICIT_WAIT_TIMEOUT

@pytest.fixture(scope="function")
def driver(request):
    """Initializes and yields Appium UiAutomator2 driver for each test."""
    options = UiAutomator2Options()
    for key, value in DESIRED_CAPABILITIES.items():
        options.set_capability(key, value)

    driver_instance = webdriver.Remote(APPIUM_SERVER_URL, options=options)
    driver_instance.implicitly_wait(IMPLICIT_WAIT_TIMEOUT)

    # Attach driver to request node for screenshot hook
    request.node.driver = driver_instance

    yield driver_instance

    # Teardown
    try:
        driver_instance.quit()
    except Exception:
        pass

@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
    """Takes a screenshot on failure and attaches it to test reports."""
    outcome = yield
    report = outcome.get_result()

    if report.when == "call" and report.failed:
        driver = getattr(item, "driver", None)
        if driver:
            screenshots_dir = os.path.join(os.path.dirname(__file__), "screenshots")
            os.makedirs(screenshots_dir, exist_ok=True)
            timestamp = time.strftime("%Y%m%d_%H%M%S")
            file_name = f"{item.name}_{timestamp}.png"
            file_path = os.path.join(screenshots_dir, file_name)
            try:
                driver.save_screenshot(file_path)
                print(f"\n[Screenshot Saved]: {file_path}")
            except Exception as e:
                print(f"\n[Screenshot Failed]: {e}")
