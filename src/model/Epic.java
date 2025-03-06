package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    protected final ArrayList<Integer> listSubtaskID = new ArrayList<>();
    private LocalDateTime endTime = LocalDateTime.of(1, 1, 1, 0, 0);

    public Epic(String title, String description) {
        super(title, description, Status.NEW, Duration.ZERO, LocalDateTime.of(1, 1, 1, 0, 0));
    }

    public Epic(int id, String title, String description) {
        super(id, title, description, Status.NEW, Duration.ZERO, LocalDateTime.of(1, 1, 1, 0, 0));
    }

    public Epic(int id, String title, String description, Status status, Duration duration, LocalDateTime startTime,
                LocalDateTime endTime) {
        super(id, title, description, status, duration, startTime);
        this.endTime = endTime;
    }

    public void addIdSubtask(int idSubtask) {
        if (idSubtask != id) {
            listSubtaskID.add(idSubtask);
        }
    }

    public ArrayList<Integer> getSubtasksID() {
        return listSubtaskID;
    }

    public boolean isEmpty() {
        return listSubtaskID.isEmpty();
    }

    public void clearSubtasksID() {
        listSubtaskID.clear();
    }

    public void removeSubtaskID(int subtaskID) {
        listSubtaskID.remove(Integer.valueOf(subtaskID));
    }

    public void setEndTime(LocalDateTime endTime) {
        if (endTime != null) this.endTime = endTime;
    }

    public Epic getCopy() {
        Epic copyEpic = new Epic(this.id, this.title, this.description, this.status, this.duration, this.startTime, this.endTime);
        this.getSubtasksID().forEach(copyEpic::addIdSubtask);
        return copyEpic;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public String toString() {
        return getType() + " №" + id + " \"" + title + "\", описание: \"" + description + "\", статус \"" + status
                + "\", ID подзадач: " + listSubtaskID + ", начало: " + startTime.toString() + ", продолжительность: "
                + duration.toMinutes() + " мин., завершение: " + endTime.toString() + "\n";
    }
}
