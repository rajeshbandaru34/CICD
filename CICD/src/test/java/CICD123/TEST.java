package CICD123;

import org.testng.annotations.Test;
import Utilites.baseClass;

public class TEST extends baseClass {

    @Test
    public void print() {
        driver.get("https://mvnrepository.com/artifact/io.github.bonigarcia/webdrivermanager/6.0.1");
        System.out.println("Website Open Ayindi Rey");
    }
}
