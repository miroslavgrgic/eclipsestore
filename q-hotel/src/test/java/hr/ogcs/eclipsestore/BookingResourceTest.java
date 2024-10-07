package hr.ogcs.eclipsestore;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class BookingResourceTest {

    @Test
    void should_fetch_all_guests() {
        given()
          .when().get("/guests")
          .then()
             .statusCode(200)
                // FIXME fetch Guest objects
             .body(is("Hello World"));
    }

}