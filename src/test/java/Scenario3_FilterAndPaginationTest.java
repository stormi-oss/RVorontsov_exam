import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Сценарий 3: Фильтрация и пагинация
 * 1. Получить книги с фильтром по жанру
 * 2. Получить книги с пагинацией
 * 3. Получить книги с фильтром по цене
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scenario3_FilterAndPaginationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://10.82.196.214";
        RestAssured.port = 8085;
    }

    @Test
    @Order(1)
    @DisplayName("3.1 GET /books?genre=Classic — фильтр по жанру")
    void filterByGenre() {
        given()
                .queryParam("genre", "Classic")
        .when()
                .get("/books")
        .then()
                .statusCode(200)
                .body("books.genre", everyItem(equalTo("Classic")));
    }

    @Test
    @Order(2)
    @DisplayName("3.2 GET /books?page=0&size=3 — пагинация")
    void pagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 3)
        .when()
                .get("/books")
        .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(3))
                .body("books.size()", lessThanOrEqualTo(3));
    }

    @Test
    @Order(3)
    @DisplayName("3.3 GET /books?minPrice=400&maxPrice=600 — фильтр по цене")
    void filterByPrice() {
        given()
                .queryParam("minPrice", 400)
                .queryParam("maxPrice", 600)
        .when()
                .get("/books")
        .then()
                .statusCode(200)
                .body("books.price", everyItem(allOf(
                        greaterThanOrEqualTo(400),
                        lessThanOrEqualTo(600))));
    }
}
