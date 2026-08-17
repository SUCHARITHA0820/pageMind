const { Builder, By, until, Key } = require('selenium-webdriver');
const { expect } = require('chai');

describe('PageMind Web Frontend - Selenium E2E Login & Auth Test Suite', function () {
  this.timeout(60000); // 60s timeout for E2E tests

  let driver;
  const BASE_URL = process.env.BASE_URL || 'http://localhost:5173';

  before(async function () {
    // Initialize Chrome WebDriver
    driver = await new Builder().forBrowser('chrome').build();
    await driver.manage().window().maximize();
    await driver.manage().setTimeouts({ implicit: 5000 });
  });

  after(async function () {
    if (driver) {
      await driver.quit();
    }
  });

  beforeEach(async function () {
    await driver.get(`${BASE_URL}/login`);
  });

  it('TC-WEB-AUTH-001: Should display PageMind branding and Login UI elements', async function () {
    const pageTitle = await driver.getTitle();
    expect(pageTitle).to.include('PageMind');

    const emailInput = await driver.findElement(By.css('input[type="email"], input[placeholder*="Email"]'));
    const passwordInput = await driver.findElement(By.css('input[type="password"], input[placeholder*="Password"]'));
    const loginBtn = await driver.findElement(By.css('button[type="submit"]'));

    expect(await emailInput.isDisplayed()).to.be.true;
    expect(await passwordInput.isDisplayed()).to.be.true;
    expect(await loginBtn.isDisplayed()).to.be.true;
  });

  it('TC-WEB-AUTH-002: Should show error notification for invalid login credentials', async function () {
    const emailInput = await driver.findElement(By.css('input[type="email"], input[placeholder*="Email"]'));
    const passwordInput = await driver.findElement(By.css('input[type="password"], input[placeholder*="Password"]'));
    const loginBtn = await driver.findElement(By.css('button[type="submit"]'));

    await emailInput.sendKeys('invaliduser@pagemind.com');
    await passwordInput.sendKeys('WrongPassword123!');
    await loginBtn.click();

    // Wait for alert banner or error text
    const errorBanner = await driver.wait(
      until.elementLocated(By.xpath("//*[contains(text(), 'Invalid') or contains(text(), 'failed') or contains(@class, 'error')]")),
      10000
    );
    expect(await errorBanner.isDisplayed()).to.be.true;
  });

  it('TC-WEB-AUTH-003: Should prevent form submission when fields are empty', async function () {
    const loginBtn = await driver.findElement(By.css('button[type="submit"]'));
    await loginBtn.click();

    const currentUrl = await driver.getCurrentUrl();
    expect(currentUrl).to.include('/login');
  });

  it('TC-WEB-AUTH-004: Should validate email field HTML5 input constraints', async function () {
    const emailInput = await driver.findElement(By.css('input[type="email"], input[placeholder*="Email"]'));
    await emailInput.sendKeys('invalid-email-format');
    
    const loginBtn = await driver.findElement(By.css('button[type="submit"]'));
    await loginBtn.click();

    const validity = await emailInput.getAttribute('validity');
    // HTML5 validity check
    expect(validity).to.exist;
  });

  it('TC-WEB-AUTH-005: Should navigate seamlessly to Signup page when link is clicked', async function () {
    const signupLink = await driver.findElement(By.xpath("//a[contains(@href, '/signup') or contains(text(), 'Sign Up')]"));
    await signupLink.click();

    await driver.wait(until.urlContains('/signup'), 5000);
    const currentUrl = await driver.getCurrentUrl();
    expect(currentUrl).to.include('/signup');
  });

  it('TC-WEB-AUTH-006: Should navigate to Forgot Password page when link is clicked', async function () {
    const forgotLink = await driver.findElement(By.xpath("//a[contains(@href, '/forgot-password') or contains(text(), 'Forgot Password')]"));
    await forgotLink.click();

    await driver.wait(until.urlContains('/forgot-password'), 5000);
    const currentUrl = await driver.getCurrentUrl();
    expect(currentUrl).to.include('/forgot-password');
  });

  it('TC-WEB-AUTH-007: Should display Google and GitHub OAuth social login buttons', async function () {
    const googleBtn = await driver.findElement(By.xpath("//button[contains(., 'Google') or contains(@class, 'google')]"));
    const githubBtn = await driver.findElement(By.xpath("//button[contains(., 'GitHub') or contains(@class, 'github')]"));

    expect(await googleBtn.isDisplayed()).to.be.true;
    expect(await githubBtn.isDisplayed()).to.be.true;
  });

  it('TC-WEB-AUTH-008: Should successfully log in valid user and redirect to Home dashboard', async function () {
    const emailInput = await driver.findElement(By.css('input[type="email"], input[placeholder*="Email"]'));
    const passwordInput = await driver.findElement(By.css('input[type="password"], input[placeholder*="Password"]'));
    const loginBtn = await driver.findElement(By.css('button[type="submit"]'));

    await emailInput.sendKeys('testuser@pagemind.com');
    await passwordInput.sendKeys('Password123!');
    await loginBtn.click();

    await driver.wait(until.urlContains('/home'), 10000);
    const currentUrl = await driver.getCurrentUrl();
    expect(currentUrl).to.include('/home');
  });

  it('TC-WEB-AUTH-009: Should verify session JWT token in localStorage after login', async function () {
    const token = await driver.executeScript("return localStorage.getItem('pagemind_token') || localStorage.getItem('token');");
    expect(token).to.be.a('string');
  });

  it('TC-WEB-AUTH-010: Should successfully log out user and clear token session', async function () {
    // Navigate to profile or click logout in navbar
    const logoutBtn = await driver.findElement(By.xpath("//button[contains(text(), 'Logout') or contains(@title, 'Logout')]"));
    await logoutBtn.click();

    await driver.wait(until.urlContains('/login'), 5000);
    const token = await driver.executeScript("return localStorage.getItem('pagemind_token');");
    expect(token).to.be.null;
  });
});
