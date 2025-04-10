package Utilites;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.github.bonigarcia.wdm.WebDriverManager;

public class baseClass {

    public String testSuiteName;
    public String testCaseName;
    public ExtentReports extentReports;
    public ExtentSparkReporter sparkReporter;
    public WebDriver driver;
    public ExtentTest extentTest;
    public String screenShotPath;

    @BeforeClass(alwaysRun = true)
    public void setupReport(ITestContext context) {
        testSuiteName = context.getSuite().getName();
        testCaseName = context.getCurrentXmlTest().getName();

        extentReports = new ExtentReports();
        sparkReporter = new ExtentSparkReporter(System.getProperty("user.dir") +
                "/ExtentReports/" + testSuiteName + "/" + testCaseName + "/" + timeStamp() + ".html");
        extentReports.attachReporter(sparkReporter);
    }

    @Parameters("browserName")
    @BeforeMethod(alwaysRun = true)
    public void setupBrowser(String browserName, ITestContext context, Method method) {
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

        // System info
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("Website URL", "TES TCICD");

        // Test info
        Capabilities capability = ((RemoteWebDriver) driver).getCapabilities();
        String device = capability.getBrowserName() + " " +
                capability.getBrowserVersion().split("\\.")[0];

        String author = context.getCurrentXmlTest().getParameter("author");

        extentTest = extentReports.createTest(method.getName(), "Testing the CICD");
        extentTest.assignAuthor(author);
        extentTest.assignDevice(device);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result, Method method) throws IOException {
        if (result.getStatus() == ITestResult.SUCCESS) {
            extentTest.pass(method.getName() + " passed");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            extentTest.fail(method.getName() + " failed");
            screenShotPath = captureScreenshotPath(result.getTestContext().getName() + "_" +
                    result.getMethod().getMethodName());
            extentTest.addScreenCaptureFromPath(screenShotPath);
            extentTest.fail(result.getThrowable());
        } else if (result.getStatus() == ITestResult.SKIP) {
            extentTest.skip(method.getName() + " skipped");
        }

        extentTest.assignCategory(method.getAnnotation(org.testng.annotations.Test.class).groups());

        File jsonFile = new File(System.getProperty("user.dir") + "/Resources/extent-reports-config.json");
        sparkReporter.loadJSONConfig(jsonFile);

    }
    
    @AfterTest
	//close the browser
	public void closeBrowser() {
		//driver.quit();
	}
	@AfterSuite
	public void generateExtentReports() {
		extentReports.flush();
	}

    public String timeStamp() {
        LocalDateTime timeDate = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return timeDate.format(format);
    }

    public String captureScreenshotPath(String fileName) {
        TakesScreenshot shot = (TakesScreenshot) driver;
        File sourcePath = shot.getScreenshotAs(OutputType.FILE);
        String path = System.getProperty("user.dir") + "/Screenshots/" + testSuiteName + "/" + testCaseName + "/" +
                fileName + "_" + timeStamp() + ".jpg";
        File destFilePath = new File(path);

        destFilePath.getParentFile().mkdirs(); // Create directories if not exist

        try {
            FileUtils.copyFile(sourcePath, destFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return destFilePath.getAbsolutePath();
    }

    public WebDriver getDriver() {
        return driver;
    }
}
