import java.util.ArrayList;

class Epic extends Task {
    ArrayList<Integer> listSubtaskID = new ArrayList<>();

    Epic(String title, String description, Status status) {
        super(title, description, status);
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return "эпик №" + id + " \"" + title + "\", статус \"" + status;
        } else {
            return "эпик №" + id + " \"" + title + "\", статус \"" + status + "\", описание: " + description;
        }
    }
}
