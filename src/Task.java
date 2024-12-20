import java.util.Objects;

class Task {
    String title;
    String description;
    static int count = 1;
    Status status;
    final int id;

    Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
        id = count;
        count++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                status == task.status &&
                id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, status, id);
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return "задача №" + id + " \"" + title + "\", статус \"" + status + "\"";
        } else {
            return "задача №" + id + " \"" + title + "\", статус \"" + status + "\", описание: " + description;
        }
    }


}
