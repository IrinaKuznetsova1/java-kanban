package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int idEpic;

    public Subtask(String title, String description, Status status, int idEpic, Duration duration, LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
        this.idEpic = idEpic;
    }

    public Subtask(int id, String title, String description, Status status, int idEpic, Duration duration, LocalDateTime startTime) {
        this(title, description, status, idEpic, duration, startTime);
        this.id = id;
    }

    public int getIdEpic() {
        return idEpic;
    }

    public Subtask getCopy() {
        return new Subtask(this.id, this.title, this.description, this.status, this.idEpic, this.duration, this.startTime);
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }

    @Override
    public String toString() {
        return getType() + " №" + id + " \"" + title + "\", описание: \"" + description + "\", статус \"" + status
                + "\", ID эпика - " + idEpic + ", начало: " + startTime.toString() + ", продолжительность: "
                + duration.toMinutes() + " мин.\n";
    }
}

