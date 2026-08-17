import os

# Appium Server Configuration
APPIUM_SERVER_URL = os.environ.get("APPIUM_SERVER_URL", "http://127.0.0.1:4723")

# Android Application Capabilities
APK_PATH = os.path.abspath(
    os.path.join(
        os.path.dirname(__file__),
        "..",
        "android-app",
        "app",
        "build",
        "intermediates",
        "apk",
        "debug",
        "app-debug.apk"
    )
)

DESIRED_CAPABILITIES = {
    "platformName": "Android",
    "appium:automationName": "UiAutomator2",
    "appium:deviceName": "Android Emulator",
    "appium:app": APK_PATH,
    "appium:appPackage": "com.pagemind.android",
    "appium:appActivity": "com.pagemind.android.MainActivity",
    "appium:noReset": False,
    "appium:fullReset": False,
    "appium:newCommandTimeout": 300,
    "appium:autoGrantPermissions": True
}

# Explicit Wait Timeouts (seconds)
EXPLICIT_WAIT_TIMEOUT = 10
IMPLICIT_WAIT_TIMEOUT = 5
FAST_WAIT_TIMEOUT = 2
