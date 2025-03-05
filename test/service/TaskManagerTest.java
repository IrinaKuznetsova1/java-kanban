package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T tm;
    protected Task task1 = new Task("Title Task1", "Description Task1", Status.NEW, Duration.ofHours(24),
            LocalDateTime.of(2000, 2, 2, 0, 0));
    protected Epic epic1 = new Epic("Title Epic1", "Description Epic1");
    protected Subtask subtask1 = new Subtask("Title Subtask1", "Description Subtask1", Status.IN_PROGRESS,
            2, Duration.ofHours(24), LocalDateTime.of(2222, 2, 2, 0, 0));

    @BeforeEach
    protected void init() {
        final int idTask1 = tm.addNewTask(task1);
        final int idEpic1 = tm.addNewEpic(epic1);
        final int idSubtask1 = tm.addNewSubtask(subtask1);
        tm.getTask(idTask1);
        tm.getEpic(idEpic1);
        tm.getSubtask(idSubtask1);
    }

    @AfterEach
    protected void removeTasks() {
        tm.deleteTasks();
        tm.deleteEpics();
        tm.deleteSubtasks();
    }

    protected static void checkTasksFields(Task task1, Task testTask) {
        assertEquals(task1.getId(), testTask.getId(), "ID task1 и task2 не совпадают.");
        assertEquals(task1.getTitle(), testTask.getTitle(), "title task1 и task2 не совпадают.");
        assertEquals(task1.getDescription(), testTask.getDescription(), "description task1 и task2 не совпадают.");
        assertEquals(task1.getStatus(), testTask.getStatus(), "status task1 и task2 не совпадают.");
        assertEquals(task1.getType(), testTask.getType(), "type task1 и task2 не совпадают.");
        assertEquals(task1.getStartTime(), testTask.getStartTime(), "startTime task1 и task2 не совпадают.");
        assertEquals(task1.getDuration(), testTask.getDuration(), "duration task1 и task2 не совпадают.");
        assertEquals(task1.getEndTime(), testTask.getEndTime(), "endTime task1 и task2 не совпадают.");
    }

    protected static void checkEpicsFields(Epic epic1, Epic testEpic) {
        assertEquals(epic1.getId(), testEpic.getId(), "ID epic1 и epic2 не совпадают.");
        assertEquals(epic1.getTitle(), testEpic.getTitle(), "title epic1 и epic2 не совпадают.");
        assertEquals(epic1.getDescription(), testEpic.getDescription(), "description epic1 и epic2 не совпадают.");
        assertEquals(epic1.getStatus(), testEpic.getStatus(), "status epic1 и epic2 не совпадают.");
        assertEquals(epic1.getType(), testEpic.getType(), "type epic1 и epic2 не совпадают.");
        assertEquals(epic1.getStartTime(), testEpic.getStartTime(), "startTime epic1 и epic2 не совпадают.");
        assertEquals(epic1.getDuration(), testEpic.getDuration(), "duration epic1 и epic2 не совпадают.");
        assertEquals(epic1.getEndTime(), testEpic.getEndTime(), "endTime epic1 и epic2 не совпадают.");
        assertEquals(epic1.getSubtasksID().size(), testEpic.getSubtasksID().size(), "Количество подзадач epic1 и epic2 не совпадает.");
        if (!epic1.isEmpty()) {
            assertEquals(epic1.getSubtasksID().getFirst(), testEpic.getSubtasksID().getFirst(), "Подзадачи epic1 и epic2 не совпадают.");
        }
    }

    protected static void checkSubtasksFields(Subtask subtask1, Subtask testSubtask) {
        assertEquals(subtask1.getId(), testSubtask.getId(), "ID subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getTitle(), testSubtask.getTitle(), "title subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getDescription(), testSubtask.getDescription(), "description subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getStatus(), testSubtask.getStatus(), "status subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getType(), testSubtask.getType(), "type subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getIdEpic(), testSubtask.getIdEpic(), "idEpic subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getStartTime(), testSubtask.getStartTime(), "startTime subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getDuration(), testSubtask.getDuration(), "duration subtask1 и subtask2 не совпадают.");
        assertEquals(subtask1.getEndTime(), testSubtask.getEndTime(), "endTime subtask1 и subtask2 не совпадают.");
    }

    @Test
    protected void getTasks() {
        final ArrayList<Task> tasks = tm.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    protected void getEpics() {
        final ArrayList<Epic> epics = tm.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic1, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    protected void getSubtasks() {
        final ArrayList<Subtask> subtasks = tm.getSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask1, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    protected void getEpicSubtasks() {
        final List<Subtask> epicSubtasks = tm.getEpicSubtasks(epic1.getId());
        assertNotNull(epicSubtasks, "Подзадачи не возвращаются.");
        assertEquals(1, epicSubtasks.size(), "Неверное количество подзадач.");

        final Subtask subtask2 = epicSubtasks.getFirst();
        checkSubtasksFields(subtask1, subtask2);
    }

    @Test
    protected void getTask() {
        final Task task2 = tm.getTask(task1.getId());
        checkTasksFields(task1, task2);
        assertTrue(tm.getHistory().contains(task2), "Задача не сохранилась в истории просмотров");
    }

    @Test
    protected void getEpic() {
        final Epic epic2 = tm.getEpic(epic1.getId());
        checkEpicsFields(epic1, epic2);
        assertTrue(tm.getHistory().contains(epic2), "Эпик не сохранился в истории просмотров");
    }

    @Test
    protected void getSubtask() {
        final Subtask subtask2 = tm.getSubtask(subtask1.getId());
        checkSubtasksFields(subtask1, subtask2);
        assertTrue(tm.getHistory().contains(subtask2), "Подзадача не сохранилась в истории просмотров");
    }

    @Test
    protected void addNewTask() {
        final Task savedTask = tm.getTask(task1.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        checkTasksFields(task1, savedTask);
        assertTrue(tm.getHistory().contains(savedTask), "Задача не сохранилась в истории просмотров");
        assertTrue(tm.getPrioritizedTasks().contains(savedTask), "Задача не сохранилась в tasksByPriority");
    }

    @Test
    protected void addNewEpic() {
        final Epic savedEpic = tm.getEpic(epic1.getId());
        assertNotNull(savedEpic, "Эпик не найден.");
        checkEpicsFields(epic1, savedEpic);
        assertTrue(tm.getHistory().contains(savedEpic), "Эпик не сохранился в истории просмотров");
    }

    @Test
    protected void addNewSubtask() {
        final Subtask savedSubtask = tm.getSubtask(subtask1.getId());
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        checkSubtasksFields(subtask1, savedSubtask);
        assertEquals(savedSubtask.getStatus(), epic1.getStatus(), "После добавления подзадачи статус эпика не изменился");
        assertTrue(tm.getHistory().contains(savedSubtask), "Подзадача не сохранилась в истории просмотров");
        assertTrue(tm.getPrioritizedTasks().contains(savedSubtask), "Подзадача не сохранилась в tasksByPriority");
    }

    @Test
    protected void updateTask() {
        final Task updTask = new Task(task1.getId(), "update title", "update description", Status.DONE
                , Duration.ofSeconds(60), LocalDateTime.of(3333, 3, 3, 3, 3));
        tm.updateTask(updTask);
        final Task task2 = tm.getTasks().getFirst();
        checkTasksFields(updTask, task2);
        final Task task4 = tm.getPrioritizedTasks().getLast();
        checkTasksFields(updTask, task4);
    }

    @Test
    protected void updateEpic() {
        final Epic updEpic = new Epic(epic1.getId(), "update title", "update description");
        tm.updateEpic(updEpic);
        final Epic epic2 = tm.getEpics().getFirst();
        checkEpicsFields(epic1, epic2);
        final Epic epic3 = (Epic) tm.getHistory().get(1);
        checkEpicsFields(epic1, epic3);
    }

    @Test
    protected void updateSubtask() {
        final Subtask updSubtask = new Subtask(subtask1.getId(), "update title", "update description"
                , Status.DONE, epic1.getId(), Duration.ofSeconds(1), LocalDateTime.MIN);
        tm.updateSubtask(updSubtask);
        final Subtask subtask2 = tm.getSubtask(subtask1.getId());
        checkSubtasksFields(updSubtask, subtask2);
        assertEquals(updSubtask.getStatus(), epic1.getStatus(), "После обновления подзадачи статус эпика не изменился");
        final Subtask subtask4 = (Subtask) tm.getPrioritizedTasks().getFirst();
        checkSubtasksFields(updSubtask, subtask4);
    }

    @Test
    protected void deleteTask() {
        tm.deleteTask(task1.getId());
        assertFalse(tm.getTasks().contains(task1), "Задача не удалена из Map tasks");
        assertFalse(tm.getHistory().contains(task1), "Задача не удалена из historyManager");
        assertFalse(tm.getPrioritizedTasks().contains(task1), "Задача не удалена из Set tasksByPriority");
    }

    @Test
    protected void deleteEpic() {
        tm.deleteEpic(epic1.getId());
        assertFalse(tm.getEpics().contains(epic1), "Эпик не удален из Map epics");
        assertFalse(tm.getHistory().contains(epic1), "Эпик не удален из historyManager");
        assertFalse(tm.getSubtasks().contains(subtask1), "Подзадача не удалена из Map subtasks");
        assertFalse(tm.getHistory().contains(subtask1), "Подзадача не удалена из historyManager");
        assertFalse(tm.getPrioritizedTasks().contains(subtask1), "Подзадача не удалена из Set tasksByPriority");
    }

    @Test
    protected void deleteSubtask() {
        tm.deleteSubtask(subtask1.getId());
        assertFalse(tm.getSubtasks().contains(subtask1), "Подзадача не удалена из Map subtasks");
        assertFalse(tm.getHistory().contains(subtask1), "Подзадача не удалена из historyManager");
        assertFalse(tm.getPrioritizedTasks().contains(subtask1), "Подзадача не удалена из Set tasksByPriority");
        assertFalse(epic1.getSubtasksID().contains(subtask1.getId()), "Подзадача не удалена из listSubtaskID");
    }

    @Test
    protected void deleteTasks() {
        tm.deleteTasks();
        assertTrue(tm.getTasks().isEmpty(), "Задачи не удалены.");
    }

    @Test
    protected void deleteEpics() {
        tm.deleteEpics();
        assertTrue(tm.getEpics().isEmpty(), "Эпики не удалены.");
        assertTrue(tm.getSubtasks().isEmpty(), "Подзадачи не удалены.");
    }

    @Test
    protected void deleteSubtasks() {
        tm.deleteSubtasks();
        assertTrue(tm.getSubtasks().isEmpty(), "Подзадачи не удалены.");
    }

    @Test
    protected void getHistory() {
        List<Task> history = tm.getHistory();
        Task task2 = history.getFirst();
        checkTasksFields(task1, task2);
        Epic epic2 = (Epic) history.get(1);
        checkEpicsFields(epic1, epic2);
        Subtask subtask2 = (Subtask) history.getLast();
        checkSubtasksFields(subtask1, subtask2);
    }

    @Test
    protected void getPrioritizedTasks() {
        List<Task> priority = tm.getPrioritizedTasks();
        assertEquals(task1, priority.getFirst(), "task1 должна быть первой по списку");
        assertEquals(subtask1, priority.getLast(), "subtask1 должна быть последней по списку");
        Task task3 = new Task("Title Task3", "Description Task3", Status.NEW, Duration.ofHours(24)
                , LocalDateTime.of(2111, 1, 1, 0, 0));
        tm.addNewTask(task3);
        priority = tm.getPrioritizedTasks();
        assertEquals(task3, priority.get(1), "task3 должна быть в середине списка");
    }
}