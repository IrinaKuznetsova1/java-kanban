package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    File testFile;
    TaskManager tm;
    Task task;
    Epic epic;
    Subtask subtask;

    @BeforeEach
    void create() throws IOException {
        testFile = File.createTempFile("test", ".csv");
        tm = new FileBackedTaskManager(testFile);
        task = new Task("task", "desc", Status.NEW);
        epic = new Epic("epic", "desc");
        subtask = new Subtask("subtask", "desc", Status.IN_PROGRESS, 2);
        tm.addNewTask(task);
        tm.addNewEpic(epic);
        tm.addNewSubtask(subtask);
        tm.getTask(1);
        tm.getEpic(2);
        tm.getSubtask(3);
    }

    @Test
    void save() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(testFile, StandardCharsets.UTF_8));
        List<String> readLines = new ArrayList<>();
        while (br.ready()) {
            readLines.add(br.readLine());
        }
        br.close();
        System.out.println(readLines);
        assertFalse(readLines.isEmpty(), "Задачи не сохраняются в файл test.csv");
    }

    @Test
    void loadFromFile() throws IOException {
        TaskManager restoredTM = FileBackedTaskManager.loadFromFile(testFile);

        List<Task> tasks = restoredTM.getTasks();
        assertEquals(1, tasks.size(), "Task не восстановлена в Map<Integer, Task> tasks из файла test.csv");
        List<Epic> epics = restoredTM.getEpics();
        assertEquals(1, epics.size(), "Epic не восстановлен в Map<Integer, Epic> epics из файла test.csv");
        List<Subtask> subtasks = restoredTM.getSubtasks();
        assertEquals(1, subtasks.size(), "Subtask не восстановлена в Map<Integer, Subtask> subtasks из файла test.csv");

        Task task1 = restoredTM.getTask(1);
        assertEquals(task.getId(), task1.getId(), "ID task и task1 после загрузки TM из файла test.csv не совпадают.");
        Epic epic1 = restoredTM.getEpic(2);
        assertEquals(epic.getId(), epic1.getId(), "ID epic и epic1 после загрузки TM из файла test.csv не совпадают.");
        assertEquals(epic.getSubtasksID().getFirst(), epic1.getSubtasksID().getFirst(), "ID подзадач epic и epic1 после загрузки TM из файла test.csv не совпадают.");
        Subtask subtask1 = restoredTM.getSubtask(3);
        assertEquals(subtask.getId(), subtask1.getId(), "ID subtask и subtask1 после загрузки TM из файла test.csv не совпадают.");

        List<Task> testHistory = restoredTM.getHistory();
        Task task2 = testHistory.getFirst();
        assertEquals(task.getId(), task2.getId(), "ID task и task2 после загрузки истории TM из файла test.csv не совпадают.");
        Epic epic2 = (Epic) testHistory.get(1);
        assertEquals(epic.getId(), epic2.getId(), "ID epic и epic2 после загрузки истории TM из файла test.csv не совпадают.");
        Subtask subtask2 = (Subtask) testHistory.getLast();
        assertEquals(subtask.getId(), subtask2.getId(), "ID subtask и subtask2 после загрузки истории TM из файла test.csv не совпадают.");
    }

    @Test
    void loadFromEmptyFile() throws IOException {
        TaskManager restoredTM = FileBackedTaskManager.loadFromFile(File.createTempFile("Empty-test", ".csv"));
        assertTrue(restoredTM.getTasks().isEmpty(), "Map <Integer, Task> tasks не пуста.");
        assertTrue(restoredTM.getEpics().isEmpty(), "Map <Integer, Epic> epics не пуста.");
        assertTrue(restoredTM.getSubtasks().isEmpty(), "Map <Integer, Subtask> subtasks не пуста.");
        assertTrue(restoredTM.getHistory().isEmpty(), "История не пуста.");
    }

    @Test
    void updateSubtask() {
        Subtask updSubtask = new Subtask(3, subtask.getTitle(), subtask.getDescription(), Status.NEW, epic.getId());
        tm.updateSubtask(updSubtask);
        assertEquals(Status.NEW, tm.getSubtask(3).getStatus(), "Статус подзадачи не обновлен.");
        assertEquals(Status.NEW, tm.getEpic(2).getStatus(), "Статус эпика не обновлен.");

    }
}