package model;

import java.util.ArrayList;

public class Epic extends Task {
    protected final ArrayList<Integer> listSubtaskID = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    public Epic(int id, String title, String description) {
        super(id, title, description, Status.NEW);
    }

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
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

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public String toString() {
        return getType() + " №" + id + " \"" + title + "\", описание: \"" + description + "\", статус \"" + status + "\", ID подзадач: " + listSubtaskID + "\n";
    }
}
