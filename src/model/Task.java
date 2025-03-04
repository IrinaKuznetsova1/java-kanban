package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    protected String title;
    protected String description;
    protected Status status;
    protected int id;
    protected Duration duration = Duration.ZERO;
    protected LocalDateTime startTime;

    public Task(String title, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.status = status;
        setDuration(duration);
        setStartTime(startTime);
    }

    public Task(int id, String title, String description, Status status, Duration duration, LocalDateTime startTime) {
        this(title, description, status, duration, startTime);
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (this.id == 0) this.id = id;
    }

    public Type getType() {
        return Type.TASK;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        if (startTime != null)
            this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        if (duration != null)
            this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public String dateToString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return date.format(formatter);
    }

    public void copyFields(Task task) {
        setTitle(task.title);
        setId(task.id);
        setDescription(task.description);
        setStatus(task.status);
        setDuration(task.duration);
        setStartTime(task.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getType() + " №" + id + " \"" + title + "\", описание: \"" + description + "\", статус \"" + status
                + "\", начало: " + dateToString(startTime) + ", продолжительность: " + duration.toMinutes() + " мин.\n";
    }
}
