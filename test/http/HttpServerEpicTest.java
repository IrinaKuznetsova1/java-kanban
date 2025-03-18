package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Status;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;
import service.TaskManagerTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerEpicTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpServerEpicTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        class EpicListTypeToken extends TypeToken<List<Epic>> {}

        manager.addNewEpic(new Epic("Title Epic1", "Description Epic1"));
        manager.addNewEpic(new Epic("Title Epic2", "Description Epic2"));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epics = gson.fromJson(response.body(), new EpicListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(2, epics.size(), "Некорректное количество эпиков");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        final Epic epic = new Epic("Test 2", "Testing epic 2");
        final int id = manager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 200
        assertEquals(200, response.statusCode());
        epic.setId(id);
        final Epic epicFromManager = manager.getEpic(id);
        TaskManagerTest.checkEpicsFields(epic, epicFromManager);

        //проверка код ответа 404
        URI url1 = URI.create("http://localhost:8080/epics/" + 10000);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).GET().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        //проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/epics/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
        }

        final int idEpic = manager.addNewEpic(new Epic("Title Epic1", "Description Epic1"));
        manager.addNewSubtask(new Subtask("Title Subtask1", "Description Subtask1", Status.NEW, idEpic,
                Duration.ofHours(1), LocalDateTime.of(2000, 2, 2, 0, 0)));
        manager.addNewSubtask(new Subtask("Title Subtask2", "Description Subtask2", Status.DONE, idEpic,
                Duration.ofHours(1), LocalDateTime.of(2001, 2, 2, 0, 0)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + idEpic + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Subtask> epicSubtasks = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertNotNull(epicSubtasks, "Подзадачи не возвращаются");
        assertEquals(2, epicSubtasks.size(), "Некорректное количество подзадач");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        final Epic epic = new Epic("Test 2", "Testing epic 2");

        final String epicJson = gson.toJson(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 201
        assertEquals(201, response.statusCode());
        epic.setId(1);
        final List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        TaskManagerTest.checkEpicsFields(epic, epicsFromManager.getFirst());

        //проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/epics/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        final Epic epic = new Epic("Test 2", "Testing epic 2");
        final int id = manager.addNewEpic(epic);
        Epic updEpic = new Epic(id,"Test 22", "Testing epic 22");

        final String epicJson = gson.toJson(updEpic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 201
        assertEquals(201, response.statusCode());
        final Epic epicFromManager = manager.getEpic(id);
        TaskManagerTest.checkEpicsFields(updEpic, epicFromManager);

        // проверка код ответа 404
        final Epic updEpic1 = new Epic(10000,"Test 22", "Testing epic 22");
        final String epicJson1 = gson.toJson(updEpic1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson1)).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        final Epic epic = new Epic("Test 2", "Testing epic 2");
        final int id = manager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверка код ответа 200
        assertEquals(200, response.statusCode());
        final List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(0, epicsFromManager.size(), "Задача не удалена");
        final Epic epicDelete = gson.fromJson(response.body(), Epic.class);
        TaskManagerTest.checkEpicsFields(epic, epicDelete);

        // проверка код ответа 404
        URI url1 = URI.create("http://localhost:8080/epics/" + 10000000);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).DELETE().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        // проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/epics/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).DELETE().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }
}
