package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String host;
    private static Transaction transaction;
    private static Url url;
    private static Url incorrectUrl;
    private static MockWebServer mockWebServer;
    private static final int SUCCESS = 200;
    private static final int UNPROCESSABLENTITY = 422;
    private static final int FOUND = 302;
    private static final int NOTFOUND = 404;
    private static final String URLSENDPOINT = "/urls";
    private static final String CHECKSENDPOINT = "/checks";

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        host = "http://localhost:" + app.port();
        url = new Url("https://ru.hexlet.io");
        url.save();

        incorrectUrl = new Url("https://localhost:5000");
        incorrectUrl.save();

        mockWebServer = new MockWebServer();

        String expected = Files.readString(Paths.get("src", "test", "resources", "fixtures", "index.html"));

        mockWebServer.enqueue(new MockResponse().setBody(expected));

        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
    }

    @Test
    void testResponseSuccess() {
        HttpResponse<String> response = Unirest.get(host).asString();
        assertThat(response.getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    void testUrls() {
        HttpResponse<String> response = Unirest
                .get(host + URLSENDPOINT)
                .asString();

        assertThat(response.getBody()).contains(url.getName());
        assertThat(response.getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    void testShowId() {
        HttpResponse<String> response = Unirest
                .get(host + URLSENDPOINT + "/" + url.getId())
                .asString();

        assertThat(response.getStatus()).isEqualTo(SUCCESS);
        assertThat(response.getBody()).contains(url.getName());
    }

    @Test
    void testCreate() {
        String urlName = "https://flashscore.com";

        HttpResponse responsePost = Unirest
                .post(host + URLSENDPOINT)
                .field("url", urlName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(FOUND);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo(URLSENDPOINT);

        HttpResponse<String> response = Unirest
                .get(host + URLSENDPOINT)
                .asString();

        assertThat(response.getStatus()).isEqualTo(SUCCESS);
        assertThat(response.getBody()).contains("Страница успешно добавлена");

        Url queryUrl = new QUrl()
                .name.equalTo(urlName)
                .findOne();

        assertThat(queryUrl).isNotNull();
        assertThat(queryUrl.getName()).isEqualTo(urlName);
    }

    @Test
    void testCreateExistingUrl() {
        HttpResponse<String> responsePost = Unirest
                .post(host + URLSENDPOINT)
                .field("url", url.getName())
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(UNPROCESSABLENTITY);
        assertThat(responsePost.getBody()).contains("Страница уже была добавлена");
    }

    @Test
    void testCreateIncorrectUrl() {
        String incorrectUrl = "1234";
        HttpResponse<String> responsePost = Unirest
                .post(host + URLSENDPOINT)
                .field("url", incorrectUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(UNPROCESSABLENTITY);
        assertThat(responsePost.getBody()).contains("Некорректный URL");

        Url queryUrl = new QUrl()
                .name.equalTo(incorrectUrl)
                .findOne();

        assertThat(queryUrl).isNull();
    }

    @Test
    void testIncorrectId() {
        HttpResponse<String> response = Unirest
                .get(host + URLSENDPOINT + "/-1")
                .asString();
        assertThat(response.getStatus()).isEqualTo(NOTFOUND);
    }

    @Test
    void testChecks() {
        String mockDescription = "Живое онлайн сообщество программистов и разрабо";
        String mockTitle = "Хекслет — больше ";
        String mockH1 = "Онлайн-школа прог";

        HttpResponse<String> response = Unirest
                .post(host + URLSENDPOINT + "/" + url.getId() + CHECKSENDPOINT)
                .asString();

        assertThat(response.getHeaders().getFirst("Location")).isEqualTo(URLSENDPOINT + "/" + url.getId());

        String body = Unirest
                .get(host + URLSENDPOINT + "/" + url.getId())
                .asString()
                .getBody();

        assertThat(body).contains("200");
        assertThat(body).contains(mockDescription);
        assertThat(body).contains(mockH1);
        assertThat(body).contains(mockTitle);
    }

    @Test
    void incorrectCheck() {
        HttpResponse<String> responsePost = Unirest
                .post(host + URLSENDPOINT + "/" + incorrectUrl.getId() + CHECKSENDPOINT)
                .asString();

        HttpResponse<String> responseGet = Unirest
                .get(host + URLSENDPOINT + "/" + incorrectUrl.getId())
                .asString();

        assertThat(responseGet.getBody()).contains("Страница недоступна или не существует");
    }

}
