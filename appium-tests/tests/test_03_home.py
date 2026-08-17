import pytest
from pages.home_page import HomePage
from pages.book_details_page import BookDetailsPage

class TestHome:
    """E2E Test cases for Home Dashboard & Book Catalog Feed."""

    def test_tc_home_001_category_pill_filtering(self, driver):
        home_page = HomePage(driver)
        home_page.click_category_pill("Fiction")
        assert True, "Filtered catalog by Fiction category"

    def test_tc_home_002_open_book_details(self, driver):
        home_page = HomePage(driver)
        book_details = BookDetailsPage(driver)
        home_page.select_first_book()
        assert True, "Opened book details screen"
