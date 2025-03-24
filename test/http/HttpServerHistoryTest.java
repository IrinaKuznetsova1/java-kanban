package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpServerHistoryTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpServerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteEpics();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        class TaskListTypeToken extends TypeToken<List<Task>> {}

        final Task task = new Task("Title Task1", "Description Task1", Status.NEW, Duration.ofHours(1),
                LocalDateTime.of(2000, 2, 2, 0, 0));
        final int idTask = manager.addNewTask(task);
        final Epic epic = new Epic("Title Epic1", "Description Epic1");
        final int idEpic = manager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Title Subtask1", "Description Subtask1", Status.NEW, idEpic,
                Duration.ofHours(1), LocalDateTime.of(2002, 2, 2, 0, 0));
        final int idSubtask = manager.addNewSubtask(subtask);

        manager.getTask(idTask);
        manager.getEpic(idEpic);
        manager.getSubtask(idSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> history = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertNotNull(history, "История не возвращается");
        assertEquals(3, history.size(), "Некорректное количество задач в истории");
        assertEquals(idTask, history.getFirst().getId(), "Id task и id task из истории не совпадают");
        assertEquals(idEpic, history.get(1).getId(), "Id epic и id epic из истории не совпадают");
        assertEquals(idSubtask, history.getLast().getId(), "Id subtask и id subtask из истории не совпадают");
    }
}
