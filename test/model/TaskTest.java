package model;

import org.junit.jupiter.api.Test;
import service.InMemoryHistoryManager;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    public void shouldBeTasksEqualsIfEqualsId() {
        Task task1 = new Task(1, "Test Title 1", "Test Description 1", Status.NEW);
        Task task2 = new Task(1, "Test Title 2", "Test Description 2", Status.DONE);
        assertEquals(task1, task2, "Задачи не равны, если равен их id.");
    }

}