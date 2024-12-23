package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    final private HashMap<Integer, Task> tasks = new HashMap<>();
    final private HashMap<Integer, Epic> epics = new HashMap<>();
    final private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int generateID = 0;

    private int generateID() {
        return ++generateID;
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getEpicSubtasks (int epicID) {
            ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getIdEpic() == epicID) {
                subtasksList.add(subtask);
            }
        }
        return subtasksList;
    }

    public Task getTask(int ID) {
        return tasks.getOrDefault(ID, null);
    }

    public Epic getEpic(int ID) {
        return epics.getOrDefault(ID, null);
    }

    public Subtask getSubtask(int ID) {
        return subtasks.getOrDefault(ID, null);
    }

    public int addNewTask(Task task) { // непонятно, для чего возвращать id
        int id = generateID();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public int addNewEpic(Epic epic) {
        int id = generateID();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    public Integer addNewSubtask(Subtask subtask) {
        Epic epic = getEpic(subtask.getIdEpic());
        if (epic == null) {
            System.out.println("Невозможно добавить подзадачу к несуществующему эпику.");
            return -1;
        }
        int id = generateID();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addIdSubtask(id);
        calculateStatusEpic(epic);
        return id;
    }

    private void calculateStatusEpic(Epic epic) {
        if (epic.isEmpty()) {
            epic.setStatus("NEW");
            return;
        }
        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic.getId());
        String checkStatus = "";
        boolean checkNew = false;
        boolean checkDone = false;
        boolean checkInProgress = false;

        for (Subtask sub : epicSubtasks) {
            if (sub.getStatus().equals("IN_PROGRESS")) {
                checkInProgress = true;
                break;
            } else if (sub.getStatus().equals("NEW")) {
                checkNew = true;
            } else if (sub.getStatus().equals("DONE")) {
                checkDone = true;
            }
        }
        if (checkInProgress || checkNew && checkDone) {
            checkStatus = "IN_PROGRESS";
        } else if (checkNew && !checkDone) {
            checkStatus = "NEW";
        } else if (checkDone && !checkNew) {
            checkStatus = "DONE";
        }
        if (!epic.getStatus().equals(checkStatus)) {
            epic.setStatus(checkStatus);
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задачи с id " + task.getId() + " не существует.");
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        } else {
            System.out.println("Эпика с id " + epic.getId() + " не существует.");
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            calculateStatusEpic(getEpic(subtask.getIdEpic()));
        } else {
            System.out.println("Подзадачи с id " + subtask.getId() + " не существует.");
        }
    }

    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        } else {
            System.out.println("Задачи с id " + id + " не существует.");
        }
    }

    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            ArrayList<Subtask> epicSubtasks = getEpicSubtasks(id);
            for (Subtask sub : epicSubtasks) {
                subtasks.remove(sub.getId());
            }
            epics.remove(id);
        } else {
            System.out.println("Эпика с id " + id + " не существует.");
        }
    }

    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            int idEpic = getSubtask(id).getIdEpic();
            Epic epic = getEpic(idEpic);
            epic.removeSubtaskID(id);
            subtasks.remove(id);
            calculateStatusEpic(epic);
        } else {
            System.out.println("Подзадачи с id " + id + " не существует.");
        }
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteEpics() {
        epics.clear();
        deleteSubtasks();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : getEpics()) {
            epic.clearSubtasksID();
        }
    }



}
