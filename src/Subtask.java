public class Subtask extends Task {
    int idEpic;

    Subtask(String title, String description, Status status, int idEpic) {
        super(title, description, status);
        this.idEpic = idEpic;
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return "подзадача №" + id + " \"" + title + "\", статус \"" + status;
        } else {
            return "подзадача №" + id + " \"" + title + "\", статус \"" + status + "\", описание: " + description;
        }
    }
}

