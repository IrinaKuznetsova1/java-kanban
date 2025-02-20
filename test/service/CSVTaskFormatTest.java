package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVTaskFormatTest {
    Task task;
    Epic epic;
    Subtask subtask;


    @BeforeEach
    void createObjects() {
        task = new Task(1, "task", "desc", Status.NEW);
        epic = new Epic(2, "epic", "desc", Status.IN_PROGRESS);
        subtask = new Subtask(3, "subtask", "desc", Status.IN_PROGRESS, 2);
    }


    @Test
    void taskToStringAndFromString() {
        String taskToString = CSVTaskFormat.taskToString(task);
        Task task1 = CSVTaskFormat.taskFromString(taskToString);
        assertEquals(task.getId(), task1.getId(), "ID task и task1 после преобразования в строку и обратно в Task не совпадают.");
        assertEquals(task.getTitle(), task1.getTitle(), "title task и task1 после преобразования в строку и обратно в Task не совпадают.");
        assertEquals(task.getDescription(), task1.getDescription(), "description task и task1 после преобразования в строку и обратно в Task не совпадают.");
        assertEquals(task.getStatus(), task1.getStatus(), "status task и task1 после преобразования в строку и обратно в Task не совпадают.");
        assertEquals(task.getType(), task1.getType(), "type task и task1 после преобразования в строку и обратно в Task не совпадают.");


        String epicToString = CSVTaskFormat.taskToString(epic);
        Epic epic1 = (Epic) CSVTaskFormat.taskFromString(epicToString);
        assertEquals(epic.getId(), epic1.getId(), "ID epic и epic1 после преобразования в строку и обратно в Epic не совпадают.");
        assertEquals(epic.getTitle(), epic1.getTitle(), "title epic и epic1 после преобразования в строку и обратно в Epic не совпадают.");
        assertEquals(epic.getDescription(), epic1.getDescription(), "description epic и epic1 после преобразования в строку и обратно в Epic не совпадают.");
        assertEquals(epic.getStatus(), epic1.getStatus(), "status epic и epic1 после преобразования в строку и обратно в Epic не совпадают.");
        assertEquals(epic.getType(), epic1.getType(), "type epic и epic1 после преобразования в строку и обратно в Epic не совпадают.");

        String subtaskToString = CSVTaskFormat.taskToString(subtask);
        Subtask subtask1 = (Subtask) CSVTaskFormat.taskFromString(subtaskToString);
        assertEquals(subtask.getId(), subtask1.getId(), "ID subtask и subtask1 после преобразования в строку и обратно в Subtask не совпадают.");
        assertEquals(subtask.getTitle(), subtask1.getTitle(), "title subtask и subtask1 после преобразования в строку и обратно в Subtask не совпадают.");
        assertEquals(subtask.getDescription(), subtask1.getDescription(), "description subtask и subtask1 после преобразования в строку и обратно в Subtask не совпадают.");
        assertEquals(subtask.getStatus(), subtask1.getStatus(), "status subtask и subtask1 после преобразования в строку и обратно в Subtask не совпадают.");
        assertEquals(subtask.getType(), subtask1.getType(), "type subtask и subtask1 после преобразования в строку и обратно в Subtask не совпадают.");
        assertEquals(subtask.getIdEpic(), subtask1.getIdEpic(), "idEpic subtask и subtask1 после преобразования в строку и обратно в Subtask не совпадают.");
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