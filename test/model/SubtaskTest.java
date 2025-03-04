package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    public void shouldBeSubtasksEqualsIfEqualsId() {
        Subtask subtask1 = new Subtask(1, "Test Title 1", "Test Description 1", Status.NEW, 2
                , Duration.ZERO, LocalDateTime.now());
        Subtask subtask2 = new Subtask(1, "Test Title 2", "Test Description 2", Status.DONE, 3
                , Duration.ZERO, LocalDateTime.now());
        assertEquals(subtask1, subtask2, "Подзадачи не равны, если равен их id.");
    }

}