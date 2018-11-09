package tacos;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HomePageBrowserTest {
  private static HtmlUnitDriver browser;
  @LocalServerPort private int port;

  @BeforeClass
  public static void setup() {
    browser = new HtmlUnitDriver();

    browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @AfterClass
  public static void tearDown() {
    browser.quit();
  }

  @Test
  public void testHomePage() {
    String homePage = "http://localhost:" + port;
    browser.get(homePage);

    String titleText = browser.getTitle();
    assertThat(titleText, equalTo("Taco Cloud"));

    String h1Text = browser.findElementByTagName("h1").getText();
    assertThat(h1Text, equalTo("Welcome to..."));

    String imgSrc = browser.findElementByTagName("img").getAttribute("src");
    assertThat(imgSrc, equalTo(homePage + "/images/TacoCloud.png"));
  }
}
