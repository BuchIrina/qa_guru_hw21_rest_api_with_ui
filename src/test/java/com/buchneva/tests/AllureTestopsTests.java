package com.buchneva.tests;

import com.buchneva.api.AuthorizationApi;
import com.buchneva.api.CreateTestCaseApi;
import com.buchneva.models.CreateTestCaseSteps;
import com.buchneva.models.Step;
import com.codeborne.selenide.Configuration;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.ArrayList;
import java.util.List;

import static com.buchneva.api.AuthorizationApi.ALLURE_TESTOPS_SESSION;
import static com.buchneva.helpers.CustomApiListener.withCustomTemplates;
import static com.buchneva.specs.LoginSpec.*;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;

public class AllureTestopsTests {

    @BeforeAll
    static void beforeAll() {
        Configuration.baseUrl = "https://allure.autotests.cloud";
        RestAssured.baseURI = "https://allure.autotests.cloud";
        RestAssured.filters(withCustomTemplates());
    }

    public static void setCookie() {
        String authorizationCookie = new AuthorizationApi()
                .getAuthorizationCookie(USER_TOKEN, USERNAME, PASSWORD);
        open("/favicon.ico");
        getWebDriver().manage().addCookie(new Cookie(ALLURE_TESTOPS_SESSION, authorizationCookie));
    }

    @Test
    void createTestCaseStepsWithApiTest() {

        int testCaseId = new CreateTestCaseApi().getTestCaseId(USER_TOKEN, USERNAME, PASSWORD);

        Faker faker = new Faker();
        CreateTestCaseSteps steps = new CreateTestCaseSteps();
        List<Step> stepList = new ArrayList<>();
        int listSize = faker.number().numberBetween(1, 10);

        for (int i = 0; i < listSize; i++) {
            Step step = new Step();
            step.setName(faker.name().nameWithMiddle());
            stepList.add(step);
        }
        steps.setSteps(stepList);

        given()
                .log().all()
                .spec(loginRequestSpec())
                .body(steps)
                .queryParam("projectId", "1722")
                .post("/api/rs/testcase/" + testCaseId + "/scenario")
                .then()
                .statusCode(200);

        setCookie();
        open("/project/1722/test-cases/" + testCaseId);
        for (int i = 0; i < stepList.size(); i++) {
            $$(".TreeElement__node").get(i).shouldHave(text(stepList.get(i).getName()));
        }
    }
}
