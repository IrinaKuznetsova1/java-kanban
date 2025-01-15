package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    InMemoryHistoryManager historyManager;

    @BeforeEach
    public void createHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void addTask() {
        Task task1 = new Task(1, "Test Title 1", "Test Description 1", Status.NEW);
        historyManager.addTask(task1);
        final List<Task> testHistory = historyManager.getHistory();
        assertNotNull(testHistory, "Task1 не сохранен.");
        assertEquals(1, testHistory.size(), "Task1 не сохранен.");
    }

    @Test
    public void getHistory() {
        Task task1 = new Task(1, "Test Title 1", "Test Description 1", Status.NEW);
        historyManager.addTask(task1);
        final List<Task> testHistory = historyManager.getHistory();
        final Task savedTask = testHistory.getFirst();
        assertEquals(savedTask.getTitle(), task1.getTitle(), "Названия объектов не совпадают.");
        assertEquals(savedTask.getDescription(), task1.getDescription(), "Описания объектов не совпадают.");
        assertEquals(savedTask.getStatus(), task1.getStatus(), "Статусы объектов не совпадают.");
        assertEquals(savedTask.getId(), task1.getId(), "ID объектов не совпадают.");
    }

    @Test
    public void classShouldBeEqualsBeforeAndAfterAdd() {
        Task task1 = new Task(1, "Test Title 1", "Test Description 1", Status.NEW);
        Epic epic1 = new Epic(2, "Test Title 2", "Test Description 2");
        Subtask subtask1 = new Subtask(3, "Test Title 3", "Test Description 3", Status.IN_PROGRESS, 2);
        historyManager.addTask(task1);
        historyManager.addTask(epic1);
        historyManager.addTask(subtask1);
        final List<Task> testHistory = historyManager.getHistory();
        final Task savedTask = testHistory.get(0);
        assertEquals(savedTask.getClass(), Task.class, "Класс объекта изменился после сохранения.");
        final Task savedEpic = testHistory.get(1);
        assertEquals(savedEpic.getClass(), Epic.class, "Класс объекта изменился после сохранения.");
        final Task savedSubtask = testHistory.get(2);
        assertEquals(savedSubtask.getClass(), Subtask.class, "Класс объекта изменился после сохранения.");
    }

    @Test
    public void maxSizeofHistoryShouldBeTen() {
        Task task1 = new Task(1, "Test Title 1", "Test Description 1", Status.NEW);
        for (int i = 1; i <= 11; i++) {
            historyManager.addTask(task1);
        }
        assertEquals(10, historyManager.getHistory().size());

        Epic epic1 = new Epic(2, "Test Title 2", "Test Description 2");
        historyManager.addTask(epic1);
        final Task savedEpic = historyManager.getHistory().get(9);
        assertEquals(savedEpic.getId(), epic1.getId(), "ID объектов не совпадают.");
    }

}