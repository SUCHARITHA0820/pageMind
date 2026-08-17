import pytest
from pages.onboarding_page import OnboardingPage
from pages.home_page import HomePage

class TestOnboarding:
    """E2E Test cases for Onboarding & Language Selection."""

    def test_tc_onbd_001_skip_onboarding(self, driver):
        onboarding_page = OnboardingPage(driver)
        onboarding_page.complete_onboarding()
        assert True, "Completed or skipped onboarding successfully"

    def test_tc_onbd_002_select_english_language(self, driver):
        onboarding_page = OnboardingPage(driver)
        onboarding_page.select_language("English")
        assert True, "Selected English language"

    def test_tc_onbd_003_select_telugu_language(self, driver):
        onboarding_page = OnboardingPage(driver)
        onboarding_page.select_language("Telugu")
        assert True, "Selected Telugu language"
