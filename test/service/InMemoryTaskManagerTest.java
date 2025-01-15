package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    TaskManager taskManager;

    @BeforeEach
    public void createTaskManager() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void shouldBeNullIfSubtasksIdEqualsEpicsId() {
        final Subtask subtask1 = new Subtask("Test Title 1", "Test Description 1", Status.NEW, 1);
        assertNull(taskManager.addNewSubtask(subtask1), "Подзадачу можно сделать своим же эпиком.");
    }

    @Test
    public void addNewTask() {
        Task task1 = new Task("Test Title", "Test Description", Status.NEW);
        final int taskId = taskManager.addNewTask(task1);
        final Task savedTask = taskManager.getTask(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");

        final ArrayList<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    public void addNewEpic() {
        Epic epic1 = new Epic("Test Title", "Test Description");
        final int epicId = taskManager.addNewEpic(epic1);
        final Epic savedEpic = taskManager.getEpic(epicId);
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic1, savedEpic, "Эпики не совпадают.");

        final ArrayList<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    public void addNewSubtask() {
        Epic epic1 = new Epic("Test Title", "Test Description");
        final int epicId = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test Title", "Test Description", Status.NEW, epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask1);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask1, savedSubtask, "Подзадачи не совпадают.");

        final ArrayList<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask1, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    public void generateTaskId() {
        Task task1 = new Task("Test Title", "Test Description", Status.NEW);
        final int idTask1 = taskManager.addNewTask(task1);
        Task task2 = new Task(1, "Test Title2", "Test Description2", Status.DONE);
        final int idTask2 = taskManager.addNewTask(task2);
        assertNotEquals(idTask1, idTask2, "Id для task2 не сгенерирован.");
    }

    @Test
    public void shouldBeEqualsAfterAdd() {
        Epic epic1 = new Epic("Test Title", "Test Description");
        epic1.addIdSubtask(3);
        final int epicId = taskManager.addNewEpic(epic1);
        final Epic epic2 = taskManager.getEpic(epicId);
        assertEquals(epic2.getTitle(), epic1.getTitle(), "Названия объектов не совпадают.");
        assertEquals(epic2.getDescription(), epic1.getDescription(), "Описания объектов не совпадают.");
        assertEquals(epic2.getStatus(), epic1.getStatus(), "Статусы объектов не совпадают.");
        assertTrue(epic2.getSubtasksID().contains(3), "Id подзадачи не сохранен.");
    }

    @Test
    public void changeEpicStatusAfterAddSubtask() {
        Epic epic1 = new Epic("Test Title", "Test Description");
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test Title", "Test Description", Status.DONE, epic1.getId());
        taskManager.addNewSubtask(subtask1);
        assertEquals(epic1.getStatus(), Status.DONE, "Статус эпика не изменился.");
    }

    @Test
    public void updateSubtask() {
        Epic epic1 = new Epic("Test Title", "Test Description");
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test Title", "Test Description", Status.DONE, epic1.getId());
        taskManager.addNewSubtask(subtask1);
        final Subtask subtask2 = new Subtask(2, "Test Title", "Test Description", Status.IN_PROGRESS, epic1.getId());
        taskManager.updateSubtask(subtask2);
        final Status subtask1AfterUpdate = taskManager.getSubtask(2).getStatus();
        assertEquals(subtask1AfterUpdate, Status.IN_PROGRESS, "Статус после обновления подзадачи не изменился.");
        final Status epic1AfterUpdateSubtask = taskManager.getEpic(1).getStatus();
        assertEquals(epic1AfterUpdateSubtask, Status.IN_PROGRESS, "Статус эпика после обновления статуса подзадачи не изменился.");
    }

    @Test
    public void removeEpic() {
        Epic epic1 = new Epic("Test Title", "Test Description");
        final int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test Title", "Test Description", Status.DONE, epic1.getId());
        final int subtask1Id = taskManager.addNewSubtask(subtask1);
        taskManager.deleteEpic(epic1Id);
        assertNull(taskManager.getEpic(epic1Id), "Эпик не удален.");
        assertNull(taskManager.getSubtask(subtask1Id), "При удалении эпика подзадача не удаляется.");
    }
}