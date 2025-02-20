package service;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVTaskFormat {

    public static String taskToString(Task task) {
        if (task instanceof Subtask) {
            return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + ","
                    + task.getDescription() + "," + ((Subtask) task).getIdEpic();
        } else {
            return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + ","
                    + task.getDescription();
        }
    }

    public static Task taskFromString(String value) {
        final String[] taskFields = value.split(",");
        final int id = Integer.parseInt(taskFields[0]);
        final Type type = Type.valueOf(taskFields[1]);
        final String title = taskFields[2];
        final Status status = Status.valueOf(taskFields[3]);
        final String description = taskFields[4];

        return switch (type) {
            case TASK -> new Task(id, title, description, status);
            case EPIC -> new Epic(id, title, description, status);
            case SUBTASK -> {
                final int epicId = Integer.parseInt(taskFields[5]);
                yield new Subtask(id, title, description, status, epicId);
            }
        };
    }

    public static String historyToString(HistoryManager history) {
        if (history.getHistory().isEmpty()) {
            return "История пуста.";
        }
        StringBuilder ids = new StringBuilder();
        for (Task t : history.getHistory()) {
            ids.append(t.getId()).append(",");
        }
        ids.deleteCharAt(ids.length() - 1);
        return ids.toString();
    }

    public static List<Integer> historyFromString(String value) {
        if (value.equals("История пуста.")) return Collections.emptyList();
        List<Integer> idHistoryList = new ArrayList<>();
        for (String id : value.split(",")) {
            idHistoryList.add(Integer.parseInt(id));
        }
        return idHistoryList;
    }

}
