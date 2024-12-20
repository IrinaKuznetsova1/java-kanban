import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static TaskManager taskManager = new TaskManager();
    static ArrayList<String> titleDescStatus;

    public static void main(String[] args) {
        System.out.println("Вас приветствует трекер задач! Поехали!");

        while (true) {
            printMenu();
            String command = scanner.nextLine();

            switch (command) {
                case "1":
                    System.out.println("Введите номер, соответствующий типу задачи:");
                    System.out.println("1 - Задача;");
                    System.out.println("2 - Эпик;");
                    System.out.println("3 - Подзадача;");
                    String numberOfType = scanner.nextLine();
                    if (checkInput(Arrays.asList("1", "2", "3"), numberOfType)) {
                        break;
                    }
                    switch (numberOfType) {
                        case "1":
                            titleDescStatus = getTitleDescStatus(numberOfType);
                            taskManager.createObject(numberOfType, titleDescStatus, 0);
                            break;
                        case "2":
                            titleDescStatus = getTitleDescStatus(numberOfType);
                            taskManager.createObject(numberOfType, titleDescStatus, 0);
                            break;
                        case "3":
                            int idEpic = getID();
                            titleDescStatus = getTitleDescStatus(numberOfType);
                            taskManager.createObject(numberOfType, titleDescStatus, idEpic);
                    }
                    break;
                case "2":
                    System.out.println("Что Вы хотите сделать?");
                    System.out.println("1 - вывести общий список всех задач, эпиков, подзадач;");
                    System.out.println("2 - вывести список всех задач;");
                    System.out.println("3 - вывести список всех эпиков;");
                    System.out.println("4 - вывести список всех подзадач;");
                    System.out.println("5 - вывести список подзадач определенного эпика по его ID.");
                    String printNumber = scanner.nextLine();
                    if (checkInput(Arrays.asList("1", "2", "3", "4", "5"), printNumber)) {
                        break;
                    }
                    switch (printNumber) {
                        case "1":
                            taskManager.printAll();
                            break;
                        case "5":
                            int idEpic = getID();
                            taskManager.printEpicWithSubtasks(idEpic);
                            break;
                        default:
                            taskManager.printObject(printNumber);
                    }
                    break;
                case "3":
                    System.out.println("Что Вы хотите сделать?");
                    System.out.println("1 - удалить все задачи, эпики, подзадачи;");
                    System.out.println("2 - удалить все задачи;");
                    System.out.println("3 - удалить все эпики;");
                    System.out.println("4 - удалить список всех подзадач;");
                    System.out.println("5 - удалить объект по его ID;");
                    String numberRemove = scanner.nextLine();
                    if (checkInput(Arrays.asList("1", "2", "3", "4", "5"), numberRemove)) {
                        break;
                    }
                    if (numberRemove.equals("5")) {
                        int id = getID();
                        taskManager.removeByID(id);
                    } else {
                        taskManager.remove(numberRemove);
                    }
                    break;
                case "4":
                    int ID = getID();
                    taskManager.printByID(ID);
                    break;
                case "5":
                    int updateID = getID();
                    System.out.println("Введите новые данные. Если изменений нет, то просто нажмите Enter");
                    if (taskManager.findTypeByID(updateID).equals("эпик")) {
                        titleDescStatus = getTitleDescStatusForUpdate("2");
                    } else {
                        titleDescStatus = getTitleDescStatusForUpdate("1");
                    }
                    taskManager.updateObject(updateID, titleDescStatus);
                    break;
                case "6":
                    System.out.println("Программа завершена!");
                    return;
                default:
                    System.out.println("Введена неизвестная команда.");
            }
            System.out.println("-".repeat(50));
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("Выберите команду:");
        System.out.println("1 - Добавить новый объект;");
        System.out.println("2 - Получить список;");
        System.out.println("3 - Удалить;");
        System.out.println("4 - Найти по идентификатору;");
        System.out.println("5 - Обновить;");
        System.out.println("6 - Выход.");
    }

    private static ArrayList<String> getTitleDescStatus(String numberOfType) {
        ArrayList<String> titleDescStatus = new ArrayList<>();
        String newTitle = "";
        while (newTitle.isEmpty()) {
            System.out.println("Введите название задачи/эпика/подзадачи:");
            newTitle = scanner.nextLine();
        }
        titleDescStatus.add(newTitle);

        System.out.println("Введите описание задачи/эпика/подзадачи:");
        String newDescription = scanner.nextLine();
        titleDescStatus.add(newDescription);
        if (numberOfType.equals("2")) {
            return titleDescStatus;
        }

        String numberOfStatus;
        String newStatus;
        while (true) {
            System.out.println("Введите цифру, соответствующую статусу: 1 - NEW, 2 - IN_PROGRESS, 3 - DONE:");
            numberOfStatus = scanner.nextLine();
            if (checkInput(Arrays.asList("1", "2", "3"), numberOfStatus)) {
                continue;
            }
            if (numberOfStatus.equals("1")) {
                newStatus = "NEW";
                break;
            }
            if (numberOfStatus.equals("2")) {
                newStatus = "IN_PROGRESS";
                break;
            }
            if (numberOfStatus.equals("3")) {
                newStatus = "DONE";
                break;
            }
        }
        titleDescStatus.add(newStatus);
        return titleDescStatus;
    }

    private static ArrayList<String> getTitleDescStatusForUpdate(String numberOfType) {
        ArrayList<String> titleDescStatus = new ArrayList<>();
        System.out.print("Введите новое название:");
        String newTitle = scanner.nextLine();
        titleDescStatus.add(newTitle);

        System.out.print("Введите новое описание:");
        String newDescription = scanner.nextLine();
        titleDescStatus.add(newDescription);

        if (numberOfType.equals("2")) {
            return titleDescStatus;
        }

        String numberOfStatus;
        String newStatus;
        while (true) {
            System.out.print("Введите цифру, соответствующую новому статусу: 1 - NEW, 2 - IN_PROGRESS, 3 - DONE:");
            numberOfStatus = scanner.nextLine();
            if (checkInput(Arrays.asList("1", "2", "3", ""), numberOfStatus)) {
                continue;
            }
            if (numberOfStatus.equals("1")) {
                newStatus = "NEW";
                break;
            }
            if (numberOfStatus.equals("2")) {
                newStatus = "IN_PROGRESS";
                break;
            }
            if (numberOfStatus.equals("3")) {
                newStatus = "DONE";
                break;
            }
            if (numberOfStatus.isEmpty()) {
                newStatus = "";
                break;
            }
        }
        titleDescStatus.add(newStatus);
        return titleDescStatus;
    }

    public static int getID() {
        while (true) {
            System.out.print("Введите ID объекта: ");
            String idString = scanner.nextLine();
            String regex = "[0-9]+";
            if (idString.matches(regex)) {
                int id = Integer.parseInt(idString);
                if (taskManager.epics.containsKey(id) ||
                        taskManager.tasks.containsKey(id) ||
                        taskManager.subtasks.containsKey(id)) {
                    return id;
                } else {
                    System.out.println("Введен несуществующий ID.");
                }
            } else {
                System.out.println("Должно быть введено число.");
            }
        }
    }

    public static boolean checkInput(List<String> input, String enterValue) {
        if (!input.contains(enterValue)) {
            System.out.println("Некорректный ввод.");
            return true;
        } else {
            return false;
        }
    }

}
