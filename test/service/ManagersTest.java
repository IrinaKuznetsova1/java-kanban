package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    public void shouldBeNotNullAfterCreateTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "taskManager не инициализирован.");
    }

    @Test
    public void shouldBeNotNullAfterCreateHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "historyManager не инициализирован.");
    }
}
