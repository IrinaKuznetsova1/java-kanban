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

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    @BeforeEach
    protected void init() {
        tm = new InMemoryTaskManager();
        super.init();
    }

    @Test
    protected void shouldBeNullIfSubtasksIdEqualsEpicsId() {
        final Subtask subtask2 = new Subtask("Test Title 1", "Test Description 1", Status.NEW,
                1, Duration.ZERO, LocalDateTime.of(1, 1, 1, 1, 1));
        assertEquals(-1, tm.addNewSubtask(subtask2), "Подзадачу можно сделать своим же эпиком.");
    }

    @Test
    protected void generateTaskId() {
        tm.deleteTasks();
        tm.generateID = 0;
        Task task3 = new Task("Test Title2", "Test Description2", Status.DONE,
                Duration.ZERO, LocalDateTime.of(1, 1, 1, 1, 1));
        Task task2 = new Task(105, "Test Title2", "Test Description2", Status.DONE,
                Duration.ZERO, LocalDateTime.of(1, 1, 1, 1, 1));
        assertEquals(1, tm.addNewTask(task3), "ID для task3 не сгенерирован.");
        assertEquals(-1, tm.addNewTask(task2), "Task 2 с заданным вручную id сохранилась в taskManager.");
    }

    @Test
    protected void calculateStatusEpic() {
        Epic epic = new Epic("epic", "");
        int idEpic = tm.addNewEpic(epic);
        Subtask sub1 = new Subtask("sub1", "", Status.IN_PROGRESS, idEpic, Duration.ofHours(100),
                LocalDateTime.of(3000, 1, 1, 1, 1));
        Subtask sub2 = new Subtask("sub2", "", Status.IN_PROGRESS, idEpic, Duration.ofHours(100),
                LocalDateTime.of(1000, 1, 1, 1, 1));

        // Проверить расчета статуса
        tm.addNewSubtask(sub1);
        tm.addNewSubtask(sub2);
        assertEquals(sub1.getStatus(), epic.getStatus(), "Неверно рассчитан статус IN_PROGRESS");
        sub1.setStatus(Status.NEW);
        sub2.setStatus(Status.NEW);
        tm.updateSubtask(sub1);
        tm.updateSubtask(sub2);
        assertEquals(sub1.getStatus(), epic.getStatus(), "Неверно рассчитан статус NEW");
        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.DONE);
        tm.updateSubtask(sub1);
        tm.updateSubtask(sub2);
        assertEquals(sub1.getStatus(), epic.getStatus(), "Неверно рассчитан статус DONE");
        sub1.setStatus(Status.NEW);
        tm.updateSubtask(sub1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверно рассчитан статус IN_PROGRESS");

        // Проверить расчет startTime, duration, EndTime
        Subtask sub3 = new Subtask("sub3", "", Status.IN_PROGRESS, idEpic, Duration.ofHours(100),
                LocalDateTime.of(2000, 1, 1, 1, 1));
        tm.addNewSubtask(sub3);
        assertEquals(sub2.getStartTime(), epic.getStartTime(), "startTime epic рассчитывается неверно.");
        assertEquals(Duration.ofHours(300), epic.getDuration(), "duration epic рассчитывается неверно.");
        assertEquals(sub1.getEndTime(), epic.getEndTime(), "endTime epic рассчитывается неверно.");
    }

    @Test
    protected void isIntersection() {
        tm.tasksByPriority.clear();
        subtask1.setStartTime(LocalDateTime.of(22, 1, 1, 1, 1));
        tm.tasksByPriority.add(subtask1);

        // нет пересечений
        task1.setStartTime(LocalDateTime.of(11, 1, 1, 1, 1));
        assertFalse(tm.isIntersection(task1));
        task1.setStartTime(LocalDateTime.of(33, 1, 1, 1, 1));
        assertFalse(tm.isIntersection(task1));

        // есть пересечения, изначально duration task1 и subtask1 24 часа
        task1.setStartTime(LocalDateTime.of(22, 1, 1, 0, 1));
        assertTrue(tm.isIntersection(task1));
        task1.setStartTime(LocalDateTime.of(22, 1, 1, 23, 1));
        assertTrue(tm.isIntersection(task1));
        task1.setStartTime(LocalDateTime.of(22, 1, 1, 10, 1));
        task1.setDuration(Duration.ofHours(3));
        assertTrue(tm.isIntersection(task1));
        task1.setStartTime(LocalDateTime.of(22, 1, 1, 0, 1));
        task1.setDuration(Duration.ofHours(36));
        assertTrue(tm.isIntersection(task1));
    }
}

