package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected int generateID = 0;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected Set<Task> tasksByPriority = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    protected int generateID() {
        return ++generateID;
    }

    protected boolean isIntersection(Task task) {
        if (task.getStartTime() == null) return false;
        return tasksByPriority
                .stream()
                .filter(t -> !t.equals(task))
                .anyMatch(t -> !((t.getStartTime().isBefore(task.getStartTime())
                        && t.getEndTime().isBefore(task.getStartTime())) ||   // если не (t целиком раньше task
                        (t.getStartTime().isAfter(task.getStartTime())        // или t целиком позже task), то есть пересечение
                                && t.getStartTime().isAfter(task.getEndTime()))));
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
        return epics.get(epicID).getSubtasksID()
                .stream()
                .map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Task getTask(int id) {
        Optional<Task> task = Optional.ofNullable(tasks.get(id));
        if (task.isEmpty())
            return new Task(0, null, null, null, null, null);
        Task copyTask = CSVTaskFormat.copyTask(task.get());
        historyManager.addTask(task.get());
        return copyTask;
    }

    @Override
    public Epic getEpic(int id) {
        Optional<Epic> epicOptional = Optional.ofNullable(epics.get(id));
        if (epicOptional.isEmpty()) return new Epic(null, null);
        Epic copyEpic = (Epic) CSVTaskFormat.copyTask(epicOptional.get());
        epicOptional.get().getSubtasksID().forEach(copyEpic::addIdSubtask);
        historyManager.addTask(epicOptional.get());
        return copyEpic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Optional<Subtask> subtask = Optional.ofNullable(subtasks.get(id));
        if (subtask.isEmpty())
            return new Subtask(0, null, null, null, 0, null, null);
        Subtask copySubtask = (Subtask) CSVTaskFormat.copyTask(subtask.get());
        historyManager.addTask(subtask.get());
        return copySubtask;
    }

    @Override
    public int addNewTask(Task task) {
        if ((task instanceof Subtask || task instanceof Epic) || (task.getId() != 0) || isIntersection(task))
            return -1;
        int id = generateID();
        task.setId(id);
        tasks.put(id, task);
        if ((task.getStartTime() != null)) tasksByPriority.add(task);
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
        Epic epic = epics.get(subtask.getIdEpic());
        if ((subtask.getId() != 0) || (epic == null) || (isIntersection(subtask))) return -1;
        int id = generateID();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addIdSubtask(id);
        calculateEpic(epic);
        if (subtask.getStartTime() != null) tasksByPriority.add(subtask);
        return id;
    }

    protected void calculateEpic(Epic epic) {
        if (epic.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epic.getId());
        Status checkStatus;
        boolean checkNew = false;
        boolean checkDone = false;
        LocalDateTime startTime = LocalDateTime.MAX;
        Duration duration = Duration.ZERO;
        LocalDateTime endTime = LocalDateTime.MIN;

        for (Subtask sub : epicSubtasks) {
            if (sub.getStartTime().isBefore(startTime))
                startTime = sub.getStartTime();
            duration = duration.plus(sub.getDuration());
            if (sub.getStartTime().plus(sub.getDuration()).isAfter(endTime))
                endTime = sub.getStartTime().plus(sub.getDuration());
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
        epic.setStartTime(startTime);
        epic.setDuration(duration);
        epic.setEndTime(endTime);
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
    public void updateTask(Task updTask) {
        if (tasks.containsKey(updTask.getId())) {
            if (updTask.getStartTime() == null || isIntersection(updTask)) return;
            Task oldTask = tasks.get(updTask.getId());
            tasksByPriority.remove(oldTask);
            oldTask.copyFields(updTask);
            tasksByPriority.add(oldTask);
        } else {
            System.out.println("Задачи с id " + updTask.getId() + " не существует.");
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
    public void updateSubtask(Subtask updSubtask) {
        int idSub = updSubtask.getId();
        Epic epic = epics.get(updSubtask.getIdEpic());
        if (subtasks.containsKey(idSub)) {
            if (updSubtask.getStartTime() == null || isIntersection(updSubtask)) return;
            Subtask oldSubtask = subtasks.get(idSub);
            tasksByPriority.remove(oldSubtask);
            oldSubtask.copyFields(updSubtask);
            tasksByPriority.add(oldSubtask);
            calculateEpic(epic);
        } else {
            System.out.println("Подзадачи с id " + updSubtask.getId() + " не существует.");
        }
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            tasksByPriority.remove(tasks.get(id));
            historyManager.remove(id);
            tasks.remove(id);
        } else {
            System.out.println("Задачи с id " + id + " не существует.");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            ArrayList<Integer> idSubtasks = epics.get(id).getSubtasksID();
            for (Integer idSub : idSubtasks) {
                historyManager.remove(idSub);
                subtasks.remove(idSub);
            }
            tasksByPriority.remove(epics.get(id));
            historyManager.remove(id);
            epics.remove(id);
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
            tasksByPriority.remove(subtasks.get(id));
            historyManager.remove(id);
            subtasks.remove(id);
            calculateEpic(epic);
        } else {
            System.out.println("Подзадачи с id " + id + " не существует.");
        }
    }

    @Override
    public void deleteTasks() {
        tasks.keySet().forEach(id -> {
            historyManager.remove(id);
            tasksByPriority.remove(tasks.get(id));
        });
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.keySet().forEach(id -> {
            historyManager.remove(id);
            tasksByPriority.remove(epics.get(id));
        });
        epics.clear();
        subtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            tasksByPriority.remove(subtasks.get(id));
        });
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            tasksByPriority.remove(subtasks.get(id));
        });
        subtasks.clear();
        getEpics().forEach(Epic::clearSubtasksID);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(tasksByPriority);
    }
}
