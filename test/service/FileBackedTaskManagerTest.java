package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    protected static File testFile;

    @BeforeAll
    protected static void createFile() throws IOException {
        testFile = File.createTempFile("test", ".csv");
    }

    @Override
    @BeforeEach
    protected void init() {
        tm = new FileBackedTaskManager(testFile);
        super.init();
    }

    @Test
    void save() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(testFile, StandardCharsets.UTF_8));
        final List<String> readLines = br.lines().skip(1).collect(Collectors.toList());
        System.out.println(readLines);
        assertFalse(readLines.isEmpty(), "Задачи не сохраняются в файл test.csv");
    }

    @Test
    void loadFromFile() throws IOException {
        Epic epic = new Epic("Empty Epic", "");
        tm.addNewEpic(epic);
        TaskManager restoredTM = FileBackedTaskManager.loadFromFile(testFile);

        List<Task> tasks = restoredTM.getTasks();
        assertEquals(1, tasks.size(), "Task не восстановлена в Map<Integer, Task> tasks из файла test.csv");
        List<Epic> epics = restoredTM.getEpics();
        assertEquals(2, epics.size(), "Epic не восстановлен в Map<Integer, Epic> epics из файла test.csv");
        List<Subtask> subtasks = restoredTM.getSubtasks();
        assertEquals(1, subtasks.size(), "Subtask не восстановлена в Map<Integer, Subtask> subtasks из файла test.csv");

        Task task2 = restoredTM.getTask(1);
        checkTasksFields(task1, task2);

        Epic epic2 = restoredTM.getEpic(2);
        checkEpicsFields(epic1, epic2);

        Epic emptyEpic = restoredTM.getEpic(4);
        checkEpicsFields(epic, emptyEpic);

        Subtask subtask2 = restoredTM.getSubtask(3);
        checkSubtasksFields(subtask1, subtask2);

        List<Task> testHistory = restoredTM.getHistory();
        Task task3 = testHistory.getFirst();
        checkTasksFields(task1, task3);
        Epic epic3 = (Epic) testHistory.get(1);
        checkEpicsFields(epic1, epic3);
        Subtask subtask3 = (Subtask) testHistory.getLast();
        checkSubtasksFields(subtask1, subtask3);

        List<Task> priority = restoredTM.getPrioritizedTasks();
        Task task4 = priority.getFirst();
        checkTasksFields(task1, task4);
        Subtask subtask4 = (Subtask) priority.getLast();
        checkSubtasksFields(subtask1, subtask4);
    }

    @Test
    void loadFromEmptyFile() throws IOException {
        TaskManager restoredTM = FileBackedTaskManager.loadFromFile(File.createTempFile("Empty-test", ".csv"));
        assertTrue(restoredTM.getTasks().isEmpty(), "Map <Integer, Task> tasks не пуста.");
        assertTrue(restoredTM.getEpics().isEmpty(), "Map <Integer, Epic> epics не пуста.");
        assertTrue(restoredTM.getSubtasks().isEmpty(), "Map <Integer, Subtask> subtasks не пуста.");
        assertTrue(restoredTM.getHistory().isEmpty(), "История не пуста.");
        assertTrue(restoredTM.getPrioritizedTasks().isEmpty(), "Set<Task> tasksByPriority не пуст.");
    }

    @Test
    public void testException() throws IOException {
        File excFile = File.createTempFile("Exception-test", ".csv");
        excFile.setReadOnly();
        Exception exception = assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager fb = new FileBackedTaskManager(excFile);
            fb.addNewTask(task1);
        }, "Попытка записи в файл, доступного только для чтения, должна приводить к исключению.");
        assertTrue(exception.getMessage().contains("Ошибка при чтении файла - "));
    }

}
