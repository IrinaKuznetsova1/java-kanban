package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        final FileBackedTaskManager restoredTaskManager = new FileBackedTaskManager(file);
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            final List<String> readLines = br.lines().skip(1).collect(Collectors.toList());
            if (readLines.isEmpty()) return restoredTaskManager;
            final String idsHistory = readLines.removeLast();
            addTasks(readLines, idsHistory, restoredTaskManager);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла - " + e.getMessage());
        }
        return restoredTaskManager;
    }

    private static void addTasks(List<String> tasksList, String idsHistory, FileBackedTaskManager tm) {
        for (String t : tasksList) {
            final Task task = CSVTaskFormat.taskFromString(t);
            final int id = task.getId();
            if (tm.generateID < id) tm.generateID = id;
            if (task instanceof Epic epic) {
                tm.epics.put(id, epic);
            } else if (task instanceof Subtask subtask) {
                tm.subtasks.put(id, subtask);
                tm.tasksByPriority.add(subtask);
                final Epic epic = tm.epics.get(subtask.getIdEpic());
                epic.addIdSubtask(subtask.getId());
            } else {
                tm.tasks.put(id, task);
                tm.tasksByPriority.add(task);
            }
        }

        final List<Integer> idsList = CSVTaskFormat.historyFromString(idsHistory);
        if (idsList.isEmpty()) return;
        for (Integer id : idsList) {
            if (tm.tasks.containsKey(id)) {
                tm.historyManager.addTask(tm.tasks.get(id));
            }
            if (tm.epics.containsKey(id)) {
                tm.historyManager.addTask(tm.epics.get(id));
            }
            if (tm.subtasks.containsKey(id)) {
                tm.historyManager.addTask(tm.subtasks.get(id));
            }
        }
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,duration,startTime,endTime,epic\n");
            for (Task task : tasks.values()) {
                writer.write(CSVTaskFormat.taskToString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(CSVTaskFormat.taskToString(epic) + "\n");
            }
            for (Subtask sub : subtasks.values()) {
                writer.write(CSVTaskFormat.taskToString(sub) + "\n");
            }

            writer.write(CSVTaskFormat.historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла - " + e.getMessage());
        }
    }

    @Override
    public Task getTask(int id) {
        final Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public int addNewTask(Task task) {
        final int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        final int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        // добавление задач в taskManager
        Task task1 = new Task("title1", "description", Status.NEW, Duration.ofSeconds(60),
                LocalDateTime.of(1, 1, 1, 1, 1));
        taskManager.addNewTask(task1);
        Task task2 = new Task("title2", "description", Status.IN_PROGRESS, Duration.ofSeconds(60),
                LocalDateTime.of(2, 2, 2, 1, 1));
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("title3", "description");
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("title4", "description", Status.DONE, epic1.getId(),
                Duration.ofSeconds(60), LocalDateTime.of(3, 3, 3, 1, 1));
        taskManager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("title5", "description", Status.NEW, epic1.getId(),
                Duration.ofSeconds(60), LocalDateTime.of(4, 4, 4, 1, 1));
        taskManager.addNewSubtask(subtask2);
        Subtask subtask3 = new Subtask("title6", "description", Status.DONE, epic1.getId(),
                Duration.ofSeconds(60), LocalDateTime.of(5, 5, 5, 1, 1));
        taskManager.addNewSubtask(subtask3);

        Epic epic2 = new Epic("title7", "description");
        taskManager.addNewEpic(epic2);

        TaskManager restoredTM = FileBackedTaskManager.loadFromFile(Paths.get("./resources/task.csv").toFile());

        // проверить восстановление задач
        System.out.println(restoredTM.getTasks());
        System.out.println(restoredTM.getEpics());
        System.out.println(restoredTM.getSubtasks());
    }

}
