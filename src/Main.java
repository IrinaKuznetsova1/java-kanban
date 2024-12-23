import service.TaskManager;
import model.Task;
import model.Epic;
import model.Subtask;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // проверить добавление задач
        Task task1 = new Task("title1", "description", "NEW");
        taskManager.addNewTask(task1);
        Task task2 = new Task("title2", "description", "IN_PROGRESS");
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("title3", "description", "");
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("title4", "description", "DONE", epic1.getId());
        taskManager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("title5", "description", "NEW", epic1.getId());
        taskManager.addNewSubtask(subtask2);

        Epic epic2 = new Epic("title6", "description", "");
        taskManager.addNewEpic(epic2);
        Subtask subtask3 = new Subtask("title7", "description", "DONE", epic2.getId());
        taskManager.addNewSubtask(subtask3);

        // проверить сохранение задач
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        // проверить обновление задач
        task1 = new  Task(1,"title1", "description", "DONE");
        taskManager.updateTask(task1);
        System.out.println(taskManager.getTask(1));
        subtask2 = new Subtask(5,"title5", "description", "DONE", epic1.getId());
        taskManager.updateSubtask(subtask2);
        System.out.println(taskManager.getEpic(3));
        System.out.println(taskManager.getSubtask(5));

        // проверить удаление задач
        taskManager.deleteTask(2);
        System.out.println(taskManager.getTask(2));

        taskManager.deleteSubtask(5);
        System.out.println(epic1);
        System.out.println(taskManager.getSubtasks());
        System.out.println(taskManager.getSubtask(5));

        taskManager.deleteEpic(3);
        System.out.println(taskManager.getEpic(3));
        System.out.println(taskManager.getSubtasks());



    }
}
