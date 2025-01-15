package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    public void shouldBeEpicsEqualsIfEqualsId() {
        Epic epic1 = new Epic(1, "Test Title 1", "Test Description 1");
        Epic epic2 = new Epic(1, "Test Title 2", "Test Description 2");
        assertEquals(epic1, epic2, "Эпики не равны, если равен их id.");
    }

    @Test
    public void shouldBeFalseIfEpicAddToItself() {
        Epic epic = new Epic(1, "Test Title", "Test Description");
        epic.addIdSubtask(1);
        assertFalse(epic.listSubtaskID.contains(epic.getId()), "Эпик можно добавить в самого себя в виде подзадачи.");
    }

}