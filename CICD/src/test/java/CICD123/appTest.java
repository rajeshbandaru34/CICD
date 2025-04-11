package CICD123;

public class appTest extends Utilities.baseClass{

    @org.testng.annotations.Test
    public void testSomething() {
        driver.get("https://www.goibibo.com/");
        System.err.println("Web page is open sucesfully");
    }
}
