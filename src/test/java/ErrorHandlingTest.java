import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Проверка обработки ошибок: 400, 401, 404, 409
 */
public class ErrorHandlingTest {

    private static final String API_KEY = "bookstore-2026-secret";
    private static Integer existingBookId;
    private static String existingBookIsbn;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://10.82.196.214";
        RestAssured.port = 8085;

        // Находим первую существующую книгу
        Response resp = given()
                .queryParam("size", 1)
        .when()
                .get("/books")
        .then()
                .statusCode(200)
                .extract().response();

        List<Integer> ids = resp.path("books.id");
        List<String> isbns = resp.path("books.isbn");
        if (!ids.isEmpty()) {
            existingBookId = ids.get(0);
            existingBookIsbn = isbns.get(0);
        }
    }

    @Test
    @DisplayName("GET /books/{id} — 404 несуществующая книга")
    void getBookNotFound() {
        given()
        .when()
                .get("/books/{id}", 999999)
        .then()
                .statusCode(404)
                .body("error", equalTo("Book not found"));
    }

    @Test
    @DisplayName("GET /books/isbn/{isbn} — 200 существующий ISBN")
    void getBookByIsbn() {
        Assumptions.assumeTrue(existingBookIsbn != null);

        given()
        .when()
                .get("/books/isbn/{isbn}", existingBookIsbn)
        .then()
                .statusCode(200)
                .body("isbn", equalTo(existingBookIsbn));
    }

    @Test
    @DisplayName("GET /books/isbn/{isbn} — 404 несуществующий ISBN")
    void getBookByIsbnNotFound() {
        given()
        .when()
                .get("/books/isbn/{isbn}", "000-0000000000")
        .then()
                .statusCode(404)
                .body("error", equalTo("Book not found with this ISBN"));
    }

    @Test
    @DisplayName("POST /books — 401 без API Key")
    void createBookNoAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"isbn":"978-0000000001","title":"X","author":"Y","price":1}
                        """)
        .when()
                .post("/books")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("POST /books — 400 без обязательных полей")
    void createBookMissingFields() {
        given()
                .header("X-API-Key", API_KEY)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"No ISBN\"}")
        .when()
                .post("/books")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /books — 409 дубликат ISBN")
    void createBookDuplicateIsbn() {
        Assumptions.assumeTrue(existingBookIsbn != null);

        given()
                .header("X-API-Key", API_KEY)
                .contentType(ContentType.JSON)
                .body("""
                        {"isbn":"%s","title":"Dup","author":"A","price":1}
                        """.formatted(existingBookIsbn))
        .when()
                .post("/books")
        .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("POST /books/{id}/reviews — 400 невалидный рейтинг")
    void addReviewInvalidRating() {
        Assumptions.assumeTrue(existingBookId != null);

        given()
                .contentType(ContentType.JSON)
                .body("{\"rating\":10}")
        .when()
                .post("/books/{id}/reviews", existingBookId)
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /books/{id}/stock — 404 несуществующая книга")
    void stockNotFound() {
        given()
        .when()
                .get("/books/{id}/stock", 999999)
        .then()
                .statusCode(404)
                .body("error", equalTo("Book not found"));
    }
}
