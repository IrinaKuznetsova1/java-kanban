package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
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
        ArrayList<Integer> idSubtasks = getEpic(epicID).getSubtasksID();
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer id : idSubtasks) {
            subtasksList.add(subtasks.get(id));
        }
        return subtasksList;
    }

    public Task getTask(int ID) {
        return tasks.get(ID);
    }

    public Epic getEpic(int ID) {
        return epics.get(ID);
    }

    public Subtask getSubtask(int ID) {
        return subtasks.get(ID);
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
            epic.setStatus(Status.NEW);
            return;
        }
        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic.getId());
        Status checkStatus;
        boolean checkNew = false;
        boolean checkDone = false;

        for (Subtask sub : epicSubtasks) {
            if (sub.getStatus() == Status.NEW) {
                checkNew = true;
            } else if (sub.getStatus() == Status.DONE) {
                checkDone = true;
            } else {
                checkNew = false;
                checkDone = false;
                break;
            }
        }
        if (!checkDone && checkNew) {
            checkStatus = Status.NEW;
        } else if (checkDone && !checkNew) {
            checkStatus = Status.DONE;
        } else {
            checkStatus = Status.IN_PROGRESS;
        }
        if (epic.getStatus() != checkStatus) {
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

    public void updateEpic(Epic newEpic) {
        if (epics.containsKey(newEpic.getId())) {
            Epic epic = getEpic(newEpic.getId());
            epic.setTitle(newEpic.getTitle());
            epic.setDescription(newEpic.getDescription());
        } else {
            System.out.println("Эпика с id " + newEpic.getId() + " не существует.");
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
            ArrayList<Integer> idSubtasks = getEpic(id).getSubtasksID();
            for (Integer idSub : idSubtasks) {
                subtasks.remove(idSub);
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
        subtasks.clear();
    }

    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : getEpics()) {
            epic.clearSubtasksID();
        }
    }



}
