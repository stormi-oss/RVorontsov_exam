import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Сценарий 2: Покупка и отзыв
 * 1. Проверить наличие книги (GET /books/{id}/stock)
 * 2. Добавить отзыв на книгу (POST /books/{id}/reviews)
 * 3. Получить отзывы (GET /books/{id}/reviews)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scenario2_StockAndReviewTest {

    private static final String API_KEY = "bookstore-2026-secret";
    private static Integer bookId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://10.82.196.214";
        RestAssured.port = 8085;

        // Создаём книгу для тестов, чтобы не зависеть от данных на сервере
        String isbn = "978-" + UUID.randomUUID().toString().substring(0, 10);
        Response resp = given()
                .header("X-API-Key", API_KEY)
                .contentType(ContentType.JSON)
                .body("""
                        {"isbn":"%s","title":"Review Test Book","author":"Review Author","genre":"Fiction","year":2026,"price":400,"stock":10,"pages":150}
                        """.formatted(isbn))
        .when()
                .post("/books")
        .then()
                .statusCode(201)
                .extract().response();

        bookId = resp.path("id");
    }

    @AfterAll
    static void cleanup() {
        // Удаляем книгу после тестов
        if (bookId != null) {
            given()
                    .header("X-API-Key", API_KEY)
            .when()
                    .delete("/books/{id}", bookId);
        }
    }

    @Test
    @Order(1)
    @DisplayName("2.1 GET /books/{id}/stock — проверить наличие книги (200)")
    void checkBookStock() {
        given()
        .when()
                .get("/books/{id}/stock", bookId)
        .then()
                .statusCode(200)
                .body("bookId", equalTo(bookId))
                .body("title", equalTo("Review Test Book"))
                .body("stock", equalTo(10))
                .body("available", notNullValue())
                .body("inStock", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("2.2 POST /books/{id}/reviews — добавить отзыв (201)")
    void addReview() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"rating":5,"comment":"Great book!","reviewerName":"Tester"}
                        """)
        .when()
                .post("/books/{id}/reviews", bookId)
        .then()
                .statusCode(201)
                .body("rating", equalTo(5))
                .body("bookId", equalTo(bookId))
                .body("reviewId", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("2.3 GET /books/{id}/reviews — получить отзывы (200)")
    void getReviews() {
        given()
        .when()
                .get("/books/{id}/reviews", bookId)
        .then()
                .statusCode(200)
                .body("bookId", equalTo(bookId))
                .body("totalReviews", greaterThanOrEqualTo(1))
                .body("averageRating", notNullValue())
                .body("reviews", notNullValue());
    }
}
