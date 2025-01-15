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
        ArrayList<Integer> idSubtasks = getEpic(epicID).getSubtasksID();
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer id : idSubtasks) {
            subtasksList.add(subtasks.get(id));
        }
        return subtasksList;
    }

    @Override
    public Task getTask(int ID) {
        if (tasks.containsKey(ID)) historyManager.addTask(tasks.get(ID));
        return tasks.get(ID);
    }

    @Override
    public Epic getEpic(int ID) {
        if (epics.containsKey(ID)) historyManager.addTask(epics.get(ID));
        return epics.get(ID);
    }

    @Override
    public Subtask getSubtask(int ID) {
        if (subtasks.containsKey(ID)) historyManager.addTask(subtasks.get(ID));
        return subtasks.get(ID);
    }

    @Override
    public int addNewTask(Task task) {
        int id = generateID();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = generateID();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Epic epic = getEpic(subtask.getIdEpic());
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
            Epic epic = getEpic(newEpic.getId());
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
            calculateStatusEpic(getEpic(subtask.getIdEpic()));
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
    }

    @Override
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

    @Override
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

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
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
