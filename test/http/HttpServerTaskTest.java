package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Status;
import model.Task;
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

class HttpServerTaskTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpServerTaskTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        class TaskListTypeToken extends TypeToken<List<Task>> {}

        manager.addNewTask(new Task("Title Task1", "Description Task1", Status.NEW, Duration.ofHours(1),
                LocalDateTime.of(2000, 2, 2, 0, 0)));
        manager.addNewTask(new Task("Title Task2", "Description Task2", Status.IN_PROGRESS, Duration.ofHours(1),
                LocalDateTime.of(2001, 2, 2, 0, 0)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasks = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(2, tasks.size(), "Некорректное количество задач");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        final Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final int id = manager.addNewTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 200
        assertEquals(200, response.statusCode());
        task.setId(id);
        final Task taskFromManager = manager.getTask(id);
        TaskManagerTest.checkTasksFields(task, taskFromManager);

        //проверка код ответа 404
        URI url1 = URI.create("http://localhost:8080/tasks/" + 10000);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).GET().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        //проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/tasks/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        final Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));

        final String taskJson = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 201
        assertEquals(201, response.statusCode());
        task.setId(1);
        final List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        TaskManagerTest.checkTasksFields(task, tasksFromManager.getFirst());

        //проверка код ответа 406
        final Task task1 = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final String taskJson1 = gson.toJson(task1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson1)).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response1.statusCode());

        //проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/tasks/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).POST(HttpRequest.BodyPublishers.ofString(taskJson1)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        final Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final int id = manager.addNewTask(task);
        Task updTask = new Task(id,"Test 22", "Testing task 22",
                Status.DONE, Duration.ofMinutes(55), LocalDateTime.of(11,11,11,11,11));

        final String taskJson = gson.toJson(updTask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //проверка код ответа 201
        assertEquals(201, response.statusCode());
        final Task taskFromManager = manager.getTask(id);
        TaskManagerTest.checkTasksFields(updTask, taskFromManager);

        // проверка код ответа 404
        final Task updTask1 = new Task(10000,"Test 22", "Testing task 22",
                Status.DONE, Duration.ofMinutes(55), LocalDateTime.of(11,11,11,11,11));
        final String taskJson1 = gson.toJson(updTask1);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson1)).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());


        // проверка код ответа 406
        final Task task2 = new Task("Test 3", "Testing task 3",
                Status.NEW, Duration.ofMinutes(55), LocalDateTime.of(44,4,4,4,4));
        final int idTask2 = manager.addNewTask(task2);
        final Task updTask2 = new Task(idTask2,"Test 3", "Testing task 3",
                Status.NEW, Duration.ofMinutes(55), LocalDateTime.of(11,11,11,11,12));
        final String taskJson2 = gson.toJson(updTask2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson2)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        final Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.of(1,1,1,1,1));
        final int id = manager.addNewTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверка код ответа 200
        assertEquals(200, response.statusCode());
        final List<Task> tasksFromManager = manager.getTasks();
        assertEquals(0, tasksFromManager.size(), "Задача не удалена");
        final Task taskDelete = gson.fromJson(response.body(), Task.class);
        TaskManagerTest.checkTasksFields(task, taskDelete);

        // проверка код ответа 404
        URI url1 = URI.create("http://localhost:8080/tasks/" + 10000000);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url1).DELETE().build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        // проверка код ответа 400
        URI url2 = URI.create("http://localhost:8080/tasks/" + "String");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).DELETE().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
    }
}
