package tacos;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DesignAndOrderTacosBrowserTest {
  private static HtmlUnitDriver browser;
  @Autowired TestRestTemplate rest;
  @LocalServerPort private int port;

  @BeforeClass
  public static void setup() {
    browser = new HtmlUnitDriver();
    browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @AfterClass
  public static void closeBrowser() {
    browser.quit();
  }

  @Test
  public void testDesignATacoPage_HappyPath() {
    browser.get(homePageUrl());
    clickDesignATaco();
    assertDesignPageElements();
    buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    clickBuildAnotherTaco();
    buildAndSubmitATaco("Another Taco", "COTO", "CARN", "JACK", "LETC", "SRCR");
    fillInAndSubmitOrderForm();
    assertThat(browser.getCurrentUrl(), equalTo(homePageUrl()));
  }

  @Test
  public void testDesignATacoPage_EmptyOrderInfo() {
    browser.get(homePageUrl());
    clickDesignATaco();
    assertDesignPageElements();
    buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    submitEmptyOrderForm();
    fillInAndSubmitOrderForm();
    assertThat(browser.getCurrentUrl(), equalTo(homePageUrl()));
  }

  @Test
  public void testDesignATacoPage_InvalidOrderInfo() {
    browser.get(homePageUrl());
    clickDesignATaco();
    assertDesignPageElements();
    buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    submitInvalidOrderForm();
    fillInAndSubmitOrderForm();
    assertThat(browser.getCurrentUrl(), equalTo(homePageUrl()));
  }

  //
  // Browser test action methods
  //
  private void buildAndSubmitATaco(String name, String... ingredients) {
    assertDesignPageElements();

    for (String ingredient : ingredients) {
      browser.findElementByCssSelector("input[value='" + ingredient + "']").click();
    }
    browser.findElementByCssSelector("input#name").sendKeys(name);
    browser.findElementByCssSelector("form").submit();
  }

  private void assertDesignPageElements() {
    assertThat(browser.getCurrentUrl(), equalTo(designPageUrl()));
    List<WebElement> ingredientGroups = browser.findElementsByClassName("ingredient-group");
    assertThat(ingredientGroups.size(), equalTo(5));

    WebElement wrapGroup = browser.findElementByCssSelector("div.ingredient-group#wraps");
    List<WebElement> wraps = wrapGroup.findElements(By.tagName("div"));
    assertThat(wraps.size(), equalTo(2));
    assertIngredient(wrapGroup, 0, "FLTO", "Flour Tortilla");
    assertIngredient(wrapGroup, 1, "COTO", "Corn Tortilla");

    WebElement proteinGroup = browser.findElementByCssSelector("div.ingredient-group#proteins");
    List<WebElement> proteins = proteinGroup.findElements(By.tagName("div"));
    assertThat(proteins.size(), equalTo(2));
    assertIngredient(proteinGroup, 0, "GRBF", "Ground Beef");
    assertIngredient(proteinGroup, 1, "CARN", "Carnitas");

    WebElement cheeseGroup = browser.findElementByCssSelector("div.ingredient-group#cheeses");
    List<WebElement> cheeses = proteinGroup.findElements(By.tagName("div"));
    assertThat(cheeses.size(), equalTo(2));
    assertIngredient(cheeseGroup, 0, "CHED", "Cheddar");
    assertIngredient(cheeseGroup, 1, "JACK", "Monterrey Jack");

    WebElement veggieGroup = browser.findElementByCssSelector("div.ingredient-group#veggies");
    List<WebElement> veggies = proteinGroup.findElements(By.tagName("div"));
    assertThat(veggies.size(), equalTo(2));
    assertIngredient(veggieGroup, 0, "TMTO", "Diced Tomatoes");
    assertIngredient(veggieGroup, 1, "LETC", "Lettuce");

    WebElement sauceGroup = browser.findElementByCssSelector("div.ingredient-group#sauces");
    List<WebElement> sauces = proteinGroup.findElements(By.tagName("div"));
    assertThat(sauces.size(), equalTo(2));
    assertIngredient(sauceGroup, 0, "SLSA", "Salsa");
    assertIngredient(sauceGroup, 1, "SRCR", "Sour Cream");
  }

  private void fillInAndSubmitOrderForm() {
    assertThat(browser.getCurrentUrl().startsWith(orderDetailsPageUrl()), is(true));
    fillField("input#name", "Ima Hungry");
    fillField("input#street", "1234 Culinary Blvd.");
    fillField("input#city", "Foodsville");
    fillField("input#state", "CO");
    fillField("input#zip", "81019");
    fillField("input#ccNumber", "4111111111111111");
    fillField("input#ccExpiration", "10/19");
    fillField("input#ccCVV", "123");
    browser.findElementByCssSelector("form").submit();
  }

  private void submitEmptyOrderForm() {
    assertThat(browser.getCurrentUrl(), equalTo(currentOrderDetailsPageUrl()));
    browser.findElementByCssSelector("form").submit();

    assertThat(browser.getCurrentUrl(), equalTo(orderDetailsPageUrl()));

    List<String> validationErrors = getValidationErrorTexts();
    assertThat(validationErrors.size(), equalTo(9));
    assertThat(
        validationErrors.contains("Please correct the problems below and resubmit."), is(true));
    assertThat(validationErrors.contains("Name is required"), is(true));
    assertThat(validationErrors.contains("Street is required"), is(true));
    assertThat(validationErrors.contains("City is required"), is(true));
    assertThat(validationErrors.contains("State is required"), is(true));
    assertThat(validationErrors.contains("Zip code is required"), is(true));
    assertThat(validationErrors.contains("Not a valid credit card number"), is(true));
    assertThat(validationErrors.contains("Must be formatted MM/YY"), is(true));
    assertThat(validationErrors.contains("Invalid CVV"), is(true));
  }

  private List<String> getValidationErrorTexts() {
    List<WebElement> validationErrorElements = browser.findElementsByClassName("validationError");
    return validationErrorElements.stream().map(WebElement::getText).collect(Collectors.toList());
  }

  private void submitInvalidOrderForm() {
    assertThat(browser.getCurrentUrl().startsWith(orderDetailsPageUrl()), is(true));
    fillField("input#name", "I");
    fillField("input#street", "1");
    fillField("input#city", "F");
    fillField("input#state", "C");
    fillField("input#zip", "8");
    fillField("input#ccNumber", "1234432112344322");
    fillField("input#ccExpiration", "14/91");
    fillField("input#ccCVV", "1234");
    browser.findElementByCssSelector("form").submit();

    assertThat(browser.getCurrentUrl(), equalTo(orderDetailsPageUrl()));

    List<String> validationErrors = getValidationErrorTexts();
    assertThat(validationErrors.size(), equalTo(4));
    assertThat(
        validationErrors.contains("Please correct the problems below and resubmit."), is(true));
    assertThat(validationErrors.contains("Not a valid credit card number"), is(true));
    assertThat(validationErrors.contains("Must be formatted MM/YY"), is(true));
    assertThat(validationErrors.contains("Invalid CVV"), is(true));
  }

  private void fillField(String fieldName, String value) {
    WebElement field = browser.findElementByCssSelector(fieldName);
    field.clear();
    field.sendKeys(value);
  }

  private void assertIngredient(
      WebElement ingredientGroup, int ingredientIdx, String id, String name) {
    List<WebElement> proteins = ingredientGroup.findElements(By.tagName("div"));
    WebElement ingredient = proteins.get(ingredientIdx);
    assertThat(ingredient.findElement(By.tagName("input")).getAttribute("value"), equalTo(id));
    assertThat(ingredient.findElement(By.tagName("span")).getText(), equalTo(name));
  }

  private void clickDesignATaco() {
    assertThat(browser.getCurrentUrl(), equalTo(homePageUrl()));

    browser.findElementByCssSelector("a[id='design']").click();
  }

  private void clickBuildAnotherTaco() {
    assertThat(browser.getCurrentUrl().startsWith(orderDetailsPageUrl()), is(true));
    browser.findElementByCssSelector("a[id='another']").click();
  }

  //
  // URL helper methods
  //
  private String designPageUrl() {
    return homePageUrl() + "design";
  }

  private String homePageUrl() {
    return "http://localhost:" + port + "/";
  }

  private String orderDetailsPageUrl() {
    return homePageUrl() + "orders";
  }

  private String currentOrderDetailsPageUrl() {
    return homePageUrl() + "orders/current";
  }
}
