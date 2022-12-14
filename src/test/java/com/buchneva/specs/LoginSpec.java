package com.buchneva.specs;

import com.buchneva.api.AuthorizationApi;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import static com.buchneva.api.AuthorizationApi.ALLURE_TESTOPS_SESSION;
import static com.buchneva.helpers.CustomApiListener.withCustomTemplates;
import static io.restassured.http.ContentType.JSON;

public class LoginSpec {
    public static final String USERNAME = "allure8",
            PASSWORD = "allure8",
            USER_TOKEN = "efd32a69-217f-41fa-9701-55f54dd55cd4";

    public static RequestSpecification loginRequestSpec() {
        AuthorizationApi authorizationApi = new AuthorizationApi();
        String xsrfToken = authorizationApi.getXsrfToken(USER_TOKEN);
        String authorizationCookie = authorizationApi.getAuthorizationCookie(USER_TOKEN, xsrfToken, USERNAME, PASSWORD);

        return RestAssured
                .given()
                .log().all()
                .filter(withCustomTemplates())
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookies("XSRF-TOKEN", xsrfToken,
                        ALLURE_TESTOPS_SESSION, authorizationCookie)
                .contentType(JSON);
    }
}
