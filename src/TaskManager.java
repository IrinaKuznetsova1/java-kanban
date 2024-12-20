import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    final HashMap<Integer, Task> tasks = new HashMap<>();
    final HashMap<Integer, Epic> epics = new HashMap<>();
    final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    String findTypeByID(int ID) {
        if (tasks.containsKey(ID)) {
            return "задача";
        } else if (epics.containsKey(ID)) {
            return "эпик";
        } else {
            return "подзадача";
        }
    }

    void createObject(String numberOfType, ArrayList<String> titleDescStatus, int idEpic) {
        String title = titleDescStatus.get(0);
        String description = titleDescStatus.get(1);
        switch (numberOfType) {
            case "1":
                String statusTask = titleDescStatus.get(2);
                Task task = new Task(title, description, Status.valueOf(statusTask));
                save(task);
                System.out.println("Задача \"" + title + "\" id " + task.id + " успешно сохранена.");
                break;
            case "2":
                Epic epic = new Epic(title, description, Status.NEW);
                save(epic);
                System.out.println("Эпик \"" + title + "\" id " + epic.id + " успешно сохранен.");
                break;
            case "3":
                String statusSubtask = titleDescStatus.get(2);
                if (!epics.containsKey(idEpic)) {
                    System.out.println("Эпика с ID " + idEpic + " не существует.");
                    return;
                }
                Subtask subtask = new Subtask(title, description, Status.valueOf(statusSubtask), idEpic);
                System.out.println("Подзадача \"" + title + "\" id " + subtask.id + " успешно сохранена.");
                save(subtask);
        }
    }

    void save(Object object) {
        if (object.getClass() == Task.class) {
            Task task = (Task) object;
            tasks.put(task.id, task);
        } else if (object.getClass() == Epic.class) {
            Epic epic = (Epic) object;
            epics.put(epic.id, epic);
        } else if (object.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) object;
            subtasks.put(subtask.id, subtask);
            Epic epic = epics.get(subtask.idEpic);
            if (!epic.listSubtaskID.contains(subtask.id)) {
                epic.listSubtaskID.add(subtask.id);
            }
            changeStatusEpic(subtask.idEpic);
        }
    }

    void changeStatusEpic(int epicID) {
        Epic epic = epics.get(epicID);
        ArrayList<Status> statuses = new ArrayList<>();
        for (Integer subID : epic.listSubtaskID) {
            statuses.add(subtasks.get(subID).status);
        }
        boolean checkNew = false;
        for (Status st : statuses) {
            if (st == Status.NEW) {
                checkNew = true;
            } else {
                checkNew = false;
                break;
            }
        }
        boolean checkDone = false;
        for (Status st : statuses) {
            if (st == Status.DONE) {
                checkDone = true;
            } else {
                checkDone = false;
                break;
            }
        }
        Status newStatus;
        if (checkNew) {
            newStatus = Status.NEW;
        } else if (checkDone) {
            newStatus = Status.DONE;
        } else {
            newStatus = Status.IN_PROGRESS;
        }
        if (epic.status != newStatus) {
            epics.remove(epic.id);
            save(epic);
            System.out.println("Статус эпика \"" + epic.title + "\" id " + epic.id + " изменен.");
        }
    }

    void printAll() {
        if (tasks.isEmpty() && epics.isEmpty()) {
            System.out.println("Список пуст!");
            return;
        }
        printObject("2");
        System.out.println("Список эпиков с подзадачами:");
        for (Epic ep : epics.values()) {
            printEpicWithSubtasks(ep.id);
        }
    }

    void printObject(String printNumber) {
        switch (printNumber) {
            case "2":
                if (tasks.isEmpty()) {
                    System.out.println("Список задач пуст.");
                    return;
                }
                System.out.println("Список всех задач:");
                for (Task task : tasks.values()) {
                    System.out.println("\t- " + task);
                }
                break;
            case "3":
                if (epics.isEmpty()) {
                    System.out.println("Список эпиков пуст.");
                    return;
                }
                System.out.println("Список всех эпиков:");
                for (Epic epic : epics.values()) {
                    System.out.println("\t- " + epic);
                }
                break;
            case "4":
                if (subtasks.isEmpty()) {
                    System.out.println("Список подзадач пуст.");
                    return;
                }
                System.out.println("Список всех подзадач:");
                for (Subtask subtask : subtasks.values()) {
                    System.out.println("\t- " + subtask);
                }
        }
    }

    void printEpicWithSubtasks(int idEpic) {
        if (epics.containsKey(idEpic)) {
            Epic epic = epics.get(idEpic);
            System.out.println(epic + ":");
            for (Integer i : epic.listSubtaskID) {
                System.out.println("\t- " + subtasks.get(i));
            }
        } else {
            System.out.println("Эпик с ID " + idEpic + " не найден.");
        }
    }

    void printByID(int ID) {
        String typeObject = findTypeByID(ID);
        switch (typeObject) {
            case "задача":
                System.out.println("Найдена " + typeObject + ":");
                System.out.println(tasks.get(ID));
                break;
            case "эпик":
                System.out.println("Найден " + typeObject + ":");
                System.out.println(epics.get(ID));
                break;
            case "подзадача":
                System.out.println("Найдена " + typeObject + ":");
                System.out.println(subtasks.get(ID));
        }
    }

    void updateObject(int ID, ArrayList<String> titleDescStatus) {
        String checkNull = "";
        for (String s : titleDescStatus) {
            checkNull += s;
        }
        if (checkNull.isEmpty()) {
            System.out.println("Вы не ввели новые данные.");
            return;
        }
        String typeObject = findTypeByID(ID);
        String title = titleDescStatus.get(0);
        String description = titleDescStatus.get(1);
        switch (typeObject) {
            case "задача":
                String status = titleDescStatus.get(2);
                Task task = tasks.get(ID);
                if (!title.isEmpty()) {
                    task.title = title;
                }
                if (!description.isEmpty()) {
                    task.description = description;
                }
                if (!status.isEmpty()) {
                    task.status = Status.valueOf(status);
                }
                tasks.remove(ID);
                save(task);
                System.out.println("Задача \"" + task.title + "\" id " + task.id + " успешно обновлена.");
                break;
            case "эпик":
                Epic epic = epics.get(ID);
                if (!title.isEmpty()) {
                    epic.title = title;
                }
                if (!description.isEmpty()) {
                    epic.description = description;
                }
                epics.remove(ID);
                save(epic);
                System.out.println("Эпик \"" + epic.title + "\" id " + epic.id + " успешно обновлен.");
                break;
            case "подзадача":
                status = titleDescStatus.get(2);
                Subtask subtask = subtasks.get(ID);
                if (!title.isEmpty()) {
                    subtask.title = title;
                }
                if (!description.isEmpty()) {
                    subtask.description = description;
                }
                if (!status.isEmpty()) {
                    subtask.status = Status.valueOf(status);
                }
                subtasks.remove(ID);
                save(subtask);
                System.out.println("Подзадача \"" + subtask.title + "\" id " + subtask.id + " успешно обновлена.");
        }
    }

    void remove(String numberRemove) {
        if (tasks.isEmpty() && epics.isEmpty() && subtasks.isEmpty()) {
            System.out.println("В трекере нет задач.");
            return;
        }
        switch (numberRemove) {
            case "1":
                tasks.clear();
                epics.clear();
                subtasks.clear();
                System.out.println("Все объекты удалены.");
                break;
            case "2":
                if (tasks.isEmpty()) {
                    System.out.println("Список задач пуст.");
                } else {
                    tasks.clear();
                    System.out.println("Все задачи удалены.");
                }
                break;
            case "3":
                if (epics.isEmpty()) {
                    System.out.println("Список эпиков пуст.");
                } else {
                    epics.clear();
                    subtasks.clear();
                    System.out.println("Все эпики удалены.");
                }
                break;
            case "4":
                if (subtasks.isEmpty()) {
                    System.out.println("Список подзадач пуст.");
                } else {
                    subtasks.clear();
                    System.out.println("Все подзадачи удалены.");
                }
        }
    }

    void removeByID(int ID) {
        String typeObject = findTypeByID(ID);
        switch (typeObject) {
            case "задача":
                System.out.println("Удалена " + tasks.get(ID));
                tasks.remove(ID);
                break;
            case "эпик":
                if (!epics.get(ID).listSubtaskID.isEmpty()) {
                    for (Integer i : epics.get(ID).listSubtaskID) {
                        subtasks.remove(i);
                    }
                }
                System.out.println("Удален " + epics.get(ID));
                epics.remove(ID);
                break;
            case "подзадача":
                subtasks.remove(ID);
                System.out.println("Удалена " + subtasks.get(ID));

        }
    }
}
