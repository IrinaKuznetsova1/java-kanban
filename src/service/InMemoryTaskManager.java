package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int generateID = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateID() {
        return ++generateID;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicID) {
        ArrayList<Integer> idSubtasks = epics.get(epicID).getSubtasksID();
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer id : idSubtasks) {
            subtasksList.add(subtasks.get(id));
        }
        return subtasksList;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) return null;
        Task copyTask = new Task(task.getId(), task.getTitle(), task.getDescription(), task.getStatus());
        historyManager.addTask(copyTask);
        return copyTask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return null;
        Epic copyEpic = new Epic(epic.getId(), epic.getTitle(), epic.getDescription(), epic.getStatus());
        for (Integer i : epic.getSubtasksID()) copyEpic.addIdSubtask(i);
        historyManager.addTask(copyEpic);
        return copyEpic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return null;
        Subtask copySubtask = new Subtask(subtask.getId(), subtask.getTitle(), subtask.getDescription(), subtask.getStatus(), subtask.getIdEpic());
        historyManager.addTask(copySubtask);
        return copySubtask;
    }

    @Override
    public int addNewTask(Task task) {
        if (task.getId() != 0) return -1;
        int id = generateID();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        if (epic.getId() != 0) return -1;
        int id = generateID();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        if (subtask.getId() != 0) return -1;
        Epic epic = epics.get(subtask.getIdEpic());
        if (epic == null) {
            System.out.println("Невозможно добавить подзадачу к несуществующему эпику.");
            return null;
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

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задачи с id " + task.getId() + " не существует.");
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (epics.containsKey(newEpic.getId())) {
            Epic epic = epics.get(newEpic.getId());
            epic.setTitle(newEpic.getTitle());
            epic.setDescription(newEpic.getDescription());
        } else {
            System.out.println("Эпика с id " + newEpic.getId() + " не существует.");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            calculateStatusEpic(epics.get(subtask.getIdEpic()));
        } else {
            System.out.println("Подзадачи с id " + subtask.getId() + " не существует.");
        }
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        } else {
            System.out.println("Задачи с id " + id + " не существует.");
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            ArrayList<Integer> idSubtasks = epics.get(id).getSubtasksID();
            for (Integer idSub : idSubtasks) {
                subtasks.remove(idSub);
                historyManager.remove(idSub);
            }
            epics.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Эпика с id " + id + " не существует.");
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            int idEpic = subtasks.get(id).getIdEpic();
            Epic epic = epics.get(idEpic);
            epic.removeSubtaskID(id);
            subtasks.remove(id);
            historyManager.remove(id);
            calculateStatusEpic(epic);
        } else {
            System.out.println("Подзадачи с id " + id + " не существует.");
        }
    }

    @Override
    public void deleteTasks() {
        for (Integer i : tasks.keySet()) {
            historyManager.remove(i);
        }
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Integer i : epics.keySet()) {
            historyManager.remove(i);
        }
        epics.clear();
        for (Integer i : subtasks.keySet()) {
            historyManager.remove(i);
        }
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Integer i : subtasks.keySet()) {
            historyManager.remove(i);
        }
        subtasks.clear();
        for (Epic epic : getEpics()) {
            epic.clearSubtasksID();
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
