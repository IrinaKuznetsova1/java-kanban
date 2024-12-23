package model;

import java.util.ArrayList;

public class Epic extends Task {
    protected ArrayList<Integer> listSubtaskID = new ArrayList<>();

    public Epic(String title, String description, String status) {
        super(title, description, "NEW");
    }

    public Epic(int id, String title, String description, String status) {
        super(id,title, description, status);
    }

    public void addIdSubtask(int idSubtask) {
        listSubtaskID.add(idSubtask);
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
    public String toString() {
        return "эпик №" + id + " \"" + title + "\", описание: " + description + "\", статус \"" + status + ", ID подзадач: " + listSubtaskID + "\n";
    }
}
