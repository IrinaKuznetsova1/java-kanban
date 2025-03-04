package service;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CSVTaskFormat {

    public static String taskToString(Task task) {
        if (task instanceof Subtask) {
            return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + ","
                    + task.getDescription() + "," + task.getDuration().toSeconds() + "," + task.getStartTime().toString()
                    + ",," + ((Subtask) task).getIdEpic();
        } else if (task instanceof Epic) {
            return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + ","
                    + task.getDescription() + "," + task.getDuration().toSeconds() + "," + task.getStartTime().toString()
                    + "," + task.getEndTime();
        } else {
            return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + ","
                    + task.getDescription() + "," + task.getDuration().toSeconds() + "," + task.getStartTime().toString();
        }
    }

    public static Task taskFromString(String value) {
        final String[] taskFields = value.split(",");
        final int id = Integer.parseInt(taskFields[0]);
        final Type type = Type.valueOf(taskFields[1]);
        final String title = taskFields[2];
        final Status status = Status.valueOf(taskFields[3]);
        final String description = taskFields[4];
        final Duration duration = Duration.ofSeconds(Integer.parseInt(taskFields[5]));
        final LocalDateTime startTime = LocalDateTime.parse(taskFields[6]);

        return switch (type) {
            case TASK -> new Task(id, title, description, status, duration, startTime);
            case EPIC -> {
                final LocalDateTime endTime = LocalDateTime.parse(taskFields[7]);
                yield new Epic(id, title, description, status, duration, startTime, endTime);
            }
            case SUBTASK -> {
                final int epicId = Integer.parseInt(taskFields[8]);
                yield new Subtask(id, title, description, status, epicId, duration, startTime);
            }
        };
    }

    public static String historyToString(HistoryManager history) {
        if (history.getHistory().isEmpty()) {
            return "История пуста.";
        }
        String historyToString = history.getHistory()
                .stream()
                .map(task -> new StringBuilder().append(task.getId()).append(","))
                .collect(Collectors.joining());
        return historyToString.substring(0, historyToString.length() - 1);
    }

    public static List<Integer> historyFromString(String value) {
        if (value.equals("История пуста.")) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public static Task copyTask(Task task) {
        return taskFromString(taskToString(task));
    }
}
