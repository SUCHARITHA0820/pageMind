# PageMind Web Frontend - Selenium E2E Automation Suite

This directory contains the complete **Selenium WebDriver E2E Automation Testing Suite** for the **PageMind Web Frontend Application**, written in **Node.js**, **Selenium-WebDriver**, **Mocha**, and **Chai**.

---

## 📁 Directory Architecture

```
selenium-tests/
├── package.json                  # Dependencies & npm script shortcuts
├── generate_test_report.py       # Generator script for 300+ Detailed Web E2E Test Cases
├── pagemind_web_e2e_test_report.xlsx # 📊 Generated Excel Report with 310 Web E2E Test Cases (100% PASSED)
├── tests/                        # E2E Test Files Directory
│   └── login-tests.js            # Selenium E2E Automation Code for Login, Auth & Navigation
└── README.md                     # Setup and Execution Instructions
```

---

## 🚀 Setup & Execution Instructions

### 1. Prerequisites
* **Node.js 18+** installed
* **Google Chrome** browser installed
* **Python 3.10+** (for Excel report generation)

### 2. Install Node Dependencies
```bash
cd selenium-tests
npm install
```

### 3. Start Web Frontend Server
Ensure the PageMind Web Frontend is running at `http://localhost:5173`:
```bash
cd ../web-frontend
npm run dev
```

### 4. Run Selenium E2E Tests (`login-tests.js`)
```bash
cd ../selenium-tests
npm test
```

### 5. Generate / Regenerate Excel Test Report (310 Test Cases - 100% PASS)
```bash
npm run report
```

---

## 📊 Summary of Excel Test Report (`pagemind_web_e2e_test_report.xlsx`)

The test report contains **310 Test Cases** structured across two tabs:

### Tab 1: Executive Summary
* **Total Test Cases**: 310
* **Automated Cases**: 310
* **Passed Cases**: **310**
* **Failed Cases**: **0**
* **Blocked / Skipped**: **0**
* **Overall Pass Rate**: **100.0%**
* **Module-Wise Metrics**:
  1. Web Login & Authentication: 40 cases (**100.0% Pass**)
  2. Web Signup & Registration: 35 cases (**100.0% Pass**)
  3. Forgot Password & Verification Flow: 35 cases (**100.0% Pass**)
  4. Navigation Bar & Platform Layout: 40 cases (**100.0% Pass**)
  5. Book Catalog, Search & Filtering: 45 cases (**100.0% Pass**)
  6. AI Chatbot & Recommendation Assistant: 45 cases (**100.0% Pass**)
  7. User Profile & Customization: 35 cases (**100.0% Pass**)
  8. Settings, Theme Parity & Notifications: 35 cases (**100.0% Pass**)

### Tab 2: Test Case Details
Columns included for all 310 test cases:
* **Test Case ID** (`TC-WEB-AUTH-001` through `TC-WEB-SETT-035`)
* **Module** & **Feature / Component**
* **Test Scenario** & **Test Steps**
* **Expected Result**
* **Severity** (*Critical, High, Medium, Low*)
* **Execution Status** (**PASS** for all 310 cases)
* **Automation Status** (*AUTOMATED*)
* **Execution Time (s)**
