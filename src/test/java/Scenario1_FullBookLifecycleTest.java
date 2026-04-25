import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Сценарий 1: Полная проверка книги
 * 1. Создать новую книгу (POST /books)
 * 2. Получить книгу по ID (GET /books/{id})
 * 3. Обновить цену (PATCH /books/{id})
 * 4. Проверить наличие (GET /books/{id}/stock)
 * 5. Удалить книгу (DELETE /books/{id})
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scenario1_FullBookLifecycleTest {

    private static final String API_KEY = "bookstore-2026-secret";
    private static Integer createdBookId;
    private static String createdBookIsbn;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://10.82.196.214";
        RestAssured.port = 8085;
    }

    @Test
    @Order(1)
    @DisplayName("1.1 POST /books — создать книгу (201)")
    void createBook() {
        createdBookIsbn = "978-" + UUID.randomUUID().toString().substring(0, 10);

        Response resp = given()
                .header("X-API-Key", API_KEY)
                .contentType(ContentType.JSON)
                .body("""
                        {"isbn":"%s","title":"Test Book","author":"Test Author","genre":"Fiction","year":2026,"price":500,"stock":5,"pages":200}
                        """.formatted(createdBookIsbn))
        .when()
                .post("/books")
        .then()
                .statusCode(201)
                .body("isbn", equalTo(createdBookIsbn))
                .body("title", equalTo("Test Book"))
                .body("author", equalTo("Test Author"))
                .body("price", equalTo(500))
                .body("id", notNullValue())
                .extract().response();

        createdBookId = resp.path("id");
    }

    @Test
    @Order(2)
    @DisplayName("1.2 GET /books/{id} — получить созданную книгу (200)")
    void getCreatedBookById() {
        Assumptions.assumeTrue(createdBookId != null);

        given()
        .when()
                .get("/books/{id}", createdBookId)
        .then()
                .statusCode(200)
                .body("id", equalTo(createdBookId))
                .body("isbn", equalTo(createdBookIsbn))
                .body("title", equalTo("Test Book"));
    }

    @Test
    @Order(3)
    @DisplayName("1.3 PATCH /books/{id} — обновить цену (200)")
    void patchBookPrice() {
        Assumptions.assumeTrue(createdBookId != null);

        given()
                .header("X-API-Key", API_KEY)
                .contentType(ContentType.JSON)
                .body("{\"price\":777}")
        .when()
                .patch("/books/{id}", createdBookId)
        .then()
                .statusCode(200)
                .body("price", equalTo(777))
                .body("title", equalTo("Test Book"));
    }

    @Test
    @Order(4)
    @DisplayName("1.4 GET /books/{id}/stock — проверить наличие (200)")
    void checkStock() {
        Assumptions.assumeTrue(createdBookId != null);

        given()
        .when()
                .get("/books/{id}/stock", createdBookId)
        .then()
                .statusCode(200)
                .body("bookId", equalTo(createdBookId))
                .body("stock", notNullValue())
                .body("available", notNullValue());
    }

    @Test
    @Order(5)
    @DisplayName("1.5 DELETE /books/{id} — удалить книгу (204) и проверить 404")
    void deleteBook() {
        Assumptions.assumeTrue(createdBookId != null);

        given()
                .header("X-API-Key", API_KEY)
        .when()
                .delete("/books/{id}", createdBookId)
        .then()
                .statusCode(204);

        given()
        .when()
                .get("/books/{id}", createdBookId)
        .then()
                .statusCode(404);
    }
}
