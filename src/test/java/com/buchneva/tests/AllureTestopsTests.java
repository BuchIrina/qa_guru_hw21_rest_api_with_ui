package com.buchneva.tests;

import com.buchneva.api.AuthorizationApi;
import com.buchneva.models.CreateTestCaseBody;
import com.codeborne.selenide.Configuration;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import static com.buchneva.api.AuthorizationApi.ALLURE_TESTOPS_SESSION;
import static com.buchneva.helpers.CustomApiListener.withCustomTemplates;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byName;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;

public class AllureTestopsTests {

    public static final String USERNAME = "allure8",
            PASSWORD = "allure8",
            USER_TOKEN = "efd32a69-217f-41fa-9701-55f54dd55cd4";


    @BeforeAll
    static void beforeAll() {
        Configuration.baseUrl = "https://allure.autotests.cloud";
        RestAssured.baseURI = "https://allure.autotests.cloud";
        RestAssured.filters(withCustomTemplates());
    }

    @Test
    void loginTest() {
        open("");
        $(byName("username")).setValue(USERNAME);
        $(byName("password")).setValue(PASSWORD).pressEnter();

        $("button[aria-label=\"User menu\"]").click();
        $(".Menu__item_info").shouldHave(text(USERNAME));
    }

    @Test
    void loginWithApiTest() {
        String authorizationCookie = new AuthorizationApi().getAuthorizationCookie(USER_TOKEN, USERNAME, PASSWORD);

        open("/favicon.ico");
        getWebDriver().manage().addCookie(new Cookie(ALLURE_TESTOPS_SESSION, authorizationCookie));

        open("");
        $("button[aria-label=\"User menu\"]").click();
        $(".Menu__item_info").shouldHave(text(USERNAME));
    }

    @Test
    void viewTestCaseWithApiTest() {
        //1. GET "/api/rs/testcase/13328/overview"
        // 2. check name is "View test case name"

        String authorizationCookie = new AuthorizationApi().getAuthorizationCookie(USER_TOKEN, USERNAME, PASSWORD);

        given()
                .log().all()
                .cookie(ALLURE_TESTOPS_SESSION, authorizationCookie)
                .get("/api/rs/testcase/13328/overview")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", is("View test case name"));
    }

    @Test
    void viewTestCaseWithUiTest() {

        String authorizationCookie = new AuthorizationApi().getAuthorizationCookie(USER_TOKEN, USERNAME, PASSWORD);

        open("/favicon.ico");
        getWebDriver().manage().addCookie(new Cookie(ALLURE_TESTOPS_SESSION, authorizationCookie));

        open("/project/1722/test-cases/13328");
        $(".TestCaseLayout__name").shouldHave(text("View test case name"));

    }

    @Test
    void createTastcaseWithApiTest() {

        /*
        1. POST "/api/rs/testcasetree/leaf"
              with body {"name":"Some random test 1"}
        2. Get tast case {id} from response {"id":13631,"name":"Some random test 1","automated":false,"external":false,"createdDate":1670922877689,"statusName":"Draft","statusColor":"#abb8c3"}
        3. Open page "/project/1722/test-cases/{id}"
        4.Check name "Some random test 1"
        */

        AuthorizationApi authorizationApi = new AuthorizationApi();
        String xsrfToken = authorizationApi.getXsrfToken(USER_TOKEN);
        String authorizationCookie = authorizationApi.getAuthorizationCookie(USER_TOKEN, xsrfToken, USERNAME, PASSWORD);

        Faker faker = new Faker();
        String testCaseName = faker.name().nameWithMiddle();

        CreateTestCaseBody testCaseBody = new CreateTestCaseBody();
        testCaseBody.setName(testCaseName);

        int testCaseId = given()
                .log().all()
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookies("XSRF-TOKEN", xsrfToken,
                        ALLURE_TESTOPS_SESSION, authorizationCookie)
                .body(testCaseBody)
                .contentType(JSON)
                .queryParam("projectId", "1722")
                .post("/api/rs/testcasetree/leaf")
                .then()
                .log().body()
                .statusCode(200)
                .body("name", is(testCaseName))
                .body("automated", is(false))
                .body("external", is(false))
                .extract()
                .path("id");

        open("/favicon.ico");
        getWebDriver().manage().addCookie(new Cookie(ALLURE_TESTOPS_SESSION, authorizationCookie));
        open("/project/1722/test-cases/" + testCaseId);
        $(".TestCaseLayout__name").shouldHave(text(testCaseName));

    }
}
