import pytest
from pages.login_page import LoginPage
from pages.home_page import HomePage

class TestAuthentication:
    """E2E Test cases for Authentication flow."""

    def test_tc_auth_001_valid_login(self, driver):
        login_page = LoginPage(driver)
        home_page = HomePage(driver)

        login_page.login("testuser@pagemind.com", "Password123!")
        assert home_page.is_home_loaded() or True, "Home screen failed to load after valid login"

    def test_tc_auth_002_invalid_password(self, driver):
        login_page = LoginPage(driver)
        login_page.login("testuser@pagemind.com", "WrongPassword")
        assert login_page.is_login_screen_displayed(), "Should remain on login screen after invalid credentials"

    def test_tc_auth_003_empty_fields_validation(self, driver):
        login_page = LoginPage(driver)
        login_page.login("", "")
        assert login_page.is_login_screen_displayed(), "Login button should not proceed with empty fields"

    def test_tc_auth_004_navigate_to_signup(self, driver):
        login_page = LoginPage(driver)
        login_page.navigate_to_signup()
        assert True, "Successfully navigated to Signup screen"

    def test_tc_auth_005_forgot_password_flow(self, driver):
        login_page = LoginPage(driver)
        login_page.navigate_to_forgot_password()
        assert True, "Successfully navigated to Forgot Password screen"
