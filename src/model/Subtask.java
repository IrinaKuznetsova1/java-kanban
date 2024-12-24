package model;

public class Subtask extends Task {
    private final int idEpic;

    public Subtask(String title, String description, Status status, int idEpic) {
        super(title, description, status);
        this.idEpic = idEpic;
    }

    public Subtask(int id, String title, String description, Status status, int idEpic) {
        this(title, description, status, idEpic);
        this.id = id;
    }

    public int getIdEpic() {
        return idEpic;
    }

    @Override
    public String toString() {
        return "подзадача №" + id + " \"" + title + "\", описание: " + description + "\", статус \"" + status  + "\" ID эпика - " + idEpic + "\n";
    }
}

