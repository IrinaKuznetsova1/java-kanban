package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    public void shouldBeTasksEqualsIfEqualsId() {
        Task task1 = new Task(1, "Test Title 1", "Test Description 1", Status.NEW
                , Duration.ZERO, LocalDateTime.now());
        Task task2 = new Task(1, "Test Title 2", "Test Description 2", Status.DONE
                , Duration.ZERO, LocalDateTime.now());
        assertEquals(task1, task2, "Задачи не равны, если равен их id.");
    }
}
