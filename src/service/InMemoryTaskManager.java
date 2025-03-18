package service;

import exceptions.FoundIntersectionException;
import exceptions.NotFoundException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
    public List<Subtask> getEpicSubtasks(int epicID) {
        if (!epics.containsKey(epicID)) throw new NotFoundException();
        return epics.get(epicID).getSubtasksID()
                .stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public Task getTask(int id) {
        if (!tasks.containsKey(id)) throw new NotFoundException();
        historyManager.addTask(tasks.get(id));
        return tasks.get(id).getCopy();
    }

    @Override
    public Epic getEpic(int id) {
        if (!epics.containsKey(id)) throw new NotFoundException();
        historyManager.addTask(epics.get(id));
        return epics.get(id).getCopy();
    }

    @Override
    public Subtask getSubtask(int id) {
        if (!subtasks.containsKey(id)) throw new NotFoundException();
        historyManager.addTask(subtasks.get(id));
        return subtasks.get(id).getCopy();
    }

    @Override
    public int addNewTask(Task task) {
        if (isIntersection(task)) throw new FoundIntersectionException();
        int id = generateID();
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null) tasksByPriority.add(task);
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
        Epic epic = epics.get(subtask.getIdEpic());
        if (epic == null) throw new NotFoundException();
        if (isIntersection(subtask)) throw new FoundIntersectionException();
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
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }
        List<Subtask> epicSubtasks = getEpicSubtasks(epic.getId());
        Status checkStatus;
        boolean checkNew = false;
        boolean checkDone = false;
        LocalDateTime startTime = LocalDateTime.MAX;
        Duration duration = Duration.ZERO;
        LocalDateTime endTime = LocalDateTime.MIN;

        for (Subtask sub : epicSubtasks) {
            if (sub.getStatus() == Status.NEW) {
                checkNew = true;
            } else if (sub.getStatus() == Status.DONE) {
                checkDone = true;
            } else {
                checkNew = false;
                checkDone = false;
            }
            if (sub.getStartTime() == null) continue;
            if (sub.getStartTime().isBefore(startTime))
                startTime = sub.getStartTime();
            if (sub.getDuration() == null) sub.setDuration(Duration.ZERO);
            duration = duration.plus(sub.getDuration());
            if (sub.getStartTime().plus(sub.getDuration()).isAfter(endTime))
                endTime = sub.getStartTime().plus(sub.getDuration());
        }
        if (startTime == LocalDateTime.MAX) {
            startTime = null;
            endTime = null;
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
        if (!tasks.containsKey(updTask.getId())) throw new NotFoundException();
        if (isIntersection(updTask)) throw new FoundIntersectionException();
        Task oldTask = tasks.get(updTask.getId());
        tasksByPriority.remove(oldTask);
        tasks.put(updTask.getId(), updTask);
        if (updTask.getStartTime() != null) tasksByPriority.add(updTask);
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (!epics.containsKey(newEpic.getId())) throw new NotFoundException();
        Epic epic = epics.get(newEpic.getId());
        epic.setTitle(newEpic.getTitle());
        epic.setDescription(newEpic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask updSubtask) {
        int idSub = updSubtask.getId();
        if (!subtasks.containsKey(idSub)) throw new NotFoundException();
        Epic epic = epics.get(updSubtask.getIdEpic());
        if (epic == null) throw new NotFoundException();
        if (isIntersection(updSubtask)) throw new FoundIntersectionException();
        Subtask oldSubtask = subtasks.get(idSub);
        tasksByPriority.remove(oldSubtask);
        subtasks.put(idSub, updSubtask);
        if (updSubtask.getStartTime() != null) tasksByPriority.add(updSubtask);
        calculateEpic(epic);
    }

    @Override
    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) throw new NotFoundException();
        tasksByPriority.remove(tasks.get(id));
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) throw new NotFoundException();
        ArrayList<Integer> idSubtasks = epics.get(id).getSubtasksID();
        for (Integer idSub : idSubtasks) {
            historyManager.remove(idSub);
            tasksByPriority.remove(subtasks.get(idSub));
            subtasks.remove(idSub);
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) throw new NotFoundException();
        int idEpic = subtasks.get(id).getIdEpic();
        Epic epic = epics.get(idEpic);
        epic.removeSubtaskID(id);
        tasksByPriority.remove(subtasks.get(id));
        historyManager.remove(id);
        subtasks.remove(id);
        calculateEpic(epic);
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
        epics.keySet().forEach(historyManager::remove);
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
        getEpics().forEach(epic -> {
            epic.clearSubtasksID();
            calculateEpic(epic);
        });
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
