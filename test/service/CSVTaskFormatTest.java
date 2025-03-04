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

class CSVTaskFormatTest {
    Task task;
    Epic epic;
    Subtask subtask;
    LocalDateTime start = LocalDateTime.of(2222, 12, 22, 22, 22);
    Duration duration = Duration.ofSeconds(1);
    LocalDateTime end = LocalDateTime.of(2222, 12, 22, 23, 22);


    @BeforeEach
    void createObjects() {
        task = new Task(1, "task", "desc", Status.NEW, duration, start);
        epic = new Epic(2, "epic", "desc", Status.IN_PROGRESS, duration, start, end);
        subtask = new Subtask(3, "subtask", "desc", Status.IN_PROGRESS, 2, duration, start);
    }

    @Test
    void taskToStringAndFromString() {
        String taskToString = CSVTaskFormat.taskToString(task);
        Task task1 = CSVTaskFormat.taskFromString(taskToString);
        TaskManagerTest.checkTasksFields(task, task1);

        epic.addIdSubtask(subtask.getId());
        String epicToString = CSVTaskFormat.taskToString(epic);
        Epic epic1 = (Epic) CSVTaskFormat.taskFromString(epicToString);
        epic1.addIdSubtask(subtask.getId());
        TaskManagerTest.checkEpicsFields(epic, epic1);

        String subtaskToString = CSVTaskFormat.taskToString(subtask);
        Subtask subtask1 = (Subtask) CSVTaskFormat.taskFromString(subtaskToString);
        TaskManagerTest.checkSubtasksFields(subtask, subtask1);
    }

    @Test
    void historyToStringAndFromString() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.addTask(task);
        historyManager.addTask(epic);
        historyManager.addTask(subtask);

        String stringHistory = CSVTaskFormat.historyToString(historyManager);
        assertEquals("1,2,3", stringHistory, "История преобразована в строку неверно");
        List<Integer> ids = CSVTaskFormat.historyFromString(stringHistory);
        assertEquals(3, ids.size(), "Строка неверно преобразована в List");
        assertEquals(1, ids.getFirst(), "Строка неверно преобразована в List");
        assertEquals(2, ids.get(1), "Строка неверно преобразована в List");
        assertEquals(3, ids.getLast(), "Строка неверно преобразована в List");
    }
}