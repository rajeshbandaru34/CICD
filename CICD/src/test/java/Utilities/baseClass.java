package Utilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.github.bonigarcia.wdm.WebDriverManager;

public class baseClass {

    protected static ExtentReports extentReports;
    protected static ExtentSparkReporter sparkReporter;
    protected static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    protected WebDriver driver;

    @BeforeSuite
    public void setupReport() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String reportPath = System.getProperty("user.dir") + "/ExtentReports/" + timeStamp + ".html";

        sparkReporter = new ExtentSparkReporter(reportPath);
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);

        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("Tester", "RAJESH");

        File jsonfile = new File(System.getProperty("user.dir") + "/Resources/extent-reports-config.json");
        sparkReporter.loadJSONConfig(jsonfile);
    }


    @Parameters("browserName")
    @BeforeMethod
    public void setupBrowser(String browserName, Method method, ITestContext context) throws Exception {

        switch (browserName.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }

        driver.manage().window().maximize();

        // Get device info
        Capabilities capability = ((RemoteWebDriver) driver).getCapabilities();
        String device = capability.getBrowserName() + " " + capability.getBrowserVersion().split("\\.")[0];
        String author = context.getCurrentXmlTest().getParameter("author");

        // Create test and assign metadata
        ExtentTest test = extentReports.createTest(method.getName(), "Test method: " + method.getName());
        test.assignAuthor(author);
        test.assignDevice(device);
        test.assignCategory(method.getAnnotation(org.testng.annotations.Test.class).groups());
        test.info("Launching browser: " + browserName);
        test.info("Maximized browser window");

        extentTest.set(test);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        ExtentTest test = extentTest.get();
        if (result.getStatus() == ITestResult.FAILURE) {
            test.fail("Test failed: " + result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            test.pass("Test passed");
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.skip("Test skipped: " + result.getThrowable());
        }

        if (driver != null) {
            driver.quit();
            test.info("Closed the browser");
        }
    }

    @AfterSuite
    public void flushReport() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
