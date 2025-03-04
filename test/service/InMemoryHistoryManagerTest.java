package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    InMemoryHistoryManager historyManager;
    final Task task1 = new Task(1, "Title Task1", "Description Task1", Status.NEW, Duration.ofHours(24),
            LocalDateTime.of(2000, 2, 2, 0, 0));
    ;
    final Epic epic1 = new Epic(2, "Title Epic1", "Description Epic1");
    ;
    final Subtask subtask1 = new Subtask(3, "Title Subtask1", "Description Subtask1", Status.IN_PROGRESS,
            2, Duration.ofHours(24), LocalDateTime.of(2222, 2, 2, 0, 0));

    @BeforeEach
    public void createHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void addTask() {
        historyManager.addTask(task1);
        final List<Task> testHistory = historyManager.getHistory();
        assertNotNull(testHistory, "Task1 не сохранен.");
        assertEquals(1, testHistory.size(), "Task1 не сохранен.");
        historyManager.addTask(task1);
        assertEquals(1, testHistory.size(), "Task1 дублируется.");
    }

    @Test
    public void getHistory() {
        historyManager.addTask(task1);
        final List<Task> testHistory = historyManager.getHistory();
        final Task savedTask = testHistory.getFirst();
        TaskManagerTest.checkTasksFields(task1, savedTask);
    }

    @Test
    public void classShouldBeEqualsBeforeAndAfterAdd() {
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
    public void checkOrderAfterAdd() {
        historyManager.addTask(subtask1);
        historyManager.addTask(epic1);
        historyManager.addTask(task1);
        historyManager.addTask(task1);
        historyManager.addTask(epic1);
        historyManager.addTask(subtask1);
        final List<Task> testHistory = historyManager.getHistory();
        assertEquals(task1, testHistory.get(0), "История не сохраняет задачи в порядке добавления.");
        assertEquals(epic1, testHistory.get(1), "История не сохраняет задачи в порядке добавления.");
        assertEquals(subtask1, testHistory.get(2), "История не сохраняет задачи в порядке добавления.");
    }

    @Test
    public void checkOrderAfterDelete() {
        final Epic epic2 = new Epic(4, "Title Epic1", "Description Epic1");
        ;
        final Subtask subtask2 = new Subtask(5, "Title Subtask1", "Description Subtask1",
                Status.IN_PROGRESS, 2, Duration.ofHours(24), LocalDateTime.of(2222, 2, 2, 0, 0));
        historyManager.addTask(task1);
        historyManager.addTask(epic1);
        historyManager.addTask(subtask1);
        historyManager.addTask(epic2);
        historyManager.addTask(subtask2);
        historyManager.remove(2);
        List<Task> testHistory = historyManager.getHistory();
        assertEquals(4, testHistory.size(), "Элемент не удален из середины списка.");
        assertEquals(subtask1, testHistory.get(1), "После удаления элемента порядок списка некорректен.");

        historyManager.remove(1);
        testHistory = historyManager.getHistory();
        assertEquals(3, testHistory.size(), "Элемент не удален из начала списка.");
        assertEquals(subtask1, testHistory.getFirst(), "После удаления первого элемента порядок списка некорректен.");

        historyManager.remove(5);
        testHistory = historyManager.getHistory();
        assertEquals(2, testHistory.size(), "Элемент не удален из конца списка.");
        assertEquals(epic2, testHistory.getLast(), "После удаления последнего элемента порядок списка некорректен.");
    }
}
