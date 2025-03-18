package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Status;
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

class HttpServerSubtaskTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    int idEpic;

    public HttpServerSubtaskTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteSubtasks();
        manager.deleteEpics();
        idEpic = manager.addNewEpic(new Epic("Title Epic1", "Description Epic1"));
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        class SubtaskListTypeToken extends TypeToken<List<Subtask>> {}

        manager.addNewSubtask(new Subtask("Title Subtask1", "Description Subtask1", Status.NEW, idEpic,
                Duration.ofHours(1), LocalDateTime.of(2000, 2, 2, 0, 0)));
        manager.addNewSubtask(new Subtask("Title Subtask2", "Description Subtask2", Status.DONE, idEpic,
                Duration.ofHours(1), LocalDateTime.of(2001, 2, 2, 0, 0)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Subtask> subtasks = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(2, subtasks.size(), "Некорректное количество подзадач");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        final Subtask subtask = new Subtask("Test 2", "Testing subtask 2", Status.NEW, idEpic,
                Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final int id = manager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 200
        assertEquals(200, response.statusCode());
        subtask.setId(id);
        final Subtask subtaskFromManager = manager.getSubtask(id);
        TaskManagerTest.checkSubtasksFields(subtask, subtaskFromManager);

        //проверка код ответа 404
        URI url1 = URI.create("http://localhost:8080/subtasks/" + 10000);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).GET().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        //проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/subtasks/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        final Subtask subtask = new Subtask("Test 2", "Testing subtask2", Status.NEW, idEpic,
                Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));

        final String subtaskJson = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 201
        assertEquals(201, response.statusCode());
        subtask.setId(2);
        final List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        TaskManagerTest.checkSubtasksFields(subtask, subtasksFromManager.getFirst());

        //проверка код ответа 406
        final Subtask subtask1 = new Subtask("Test 2", "Testing subtask 2", Status.NEW, idEpic,
                Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final String subtaskJson1 = gson.toJson(subtask1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson1)).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response1.statusCode());

        //проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/subtasks/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).POST(HttpRequest.BodyPublishers.ofString(subtaskJson1)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        final Subtask subtask = new Subtask("Test 2", "Testing subtask2", Status.NEW, idEpic,
                Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final int id = manager.addNewSubtask(subtask);
        Subtask updSubtask = new Subtask(id,"Test 22", "Testing subtask 22", Status.DONE, idEpic,
                Duration.ofMinutes(55), LocalDateTime.of(11,11,11,11,11));

        final String subtaskJson = gson.toJson(updSubtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 201
        assertEquals(201, response.statusCode());
        final Subtask subtaskFromManager = manager.getSubtask(id);
        TaskManagerTest.checkSubtasksFields(updSubtask, subtaskFromManager);

        // проверка код ответа 404
        final Subtask updSubtask1 = new Subtask(10000,"Test 22", "Testing subtask22", Status.DONE, idEpic,
                Duration.ofMinutes(55), LocalDateTime.of(11,11,11,11,11));
        final String subtaskJson1 = gson.toJson(updSubtask1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson1)).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());


        // проверка код ответа 406
        final Subtask subtask2 = new Subtask("Test 3", "Testing subtask3", Status.NEW, idEpic,
                Duration.ofMinutes(55), LocalDateTime.of(44,4,4,4,4));
        final int idSubtask2 = manager.addNewSubtask(subtask2);
        final Subtask updSubtask2 = new Subtask(idSubtask2,"Test 3", "Testing subtask 3", Status.NEW, idEpic,
                Duration.ofMinutes(55), LocalDateTime.of(11,11,11,11,12));
        final String subtaskJson2 = gson.toJson(updSubtask2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson2)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        final Subtask subtask = new Subtask("Test 2", "Testing subtask 2", Status.NEW, idEpic,
                Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final int id = manager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверка код ответа 200
        assertEquals(200, response.statusCode());
        final List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals(0, subtasksFromManager.size(), "Подзадача не удалена");
        final Subtask subtaskDelete = gson.fromJson(response.body(), Subtask.class);
        TaskManagerTest.checkSubtasksFields(subtask, subtaskDelete);

        // проверка код ответа 404
        URI url1 = URI.create("http://localhost:8080/subtasks/" + 10000000);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).DELETE().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        // проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/subtasks/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).DELETE().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }
}

