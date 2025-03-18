package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.FoundIntersectionException;
import exceptions.NotFoundException;
import model.Subtask;
import exceptions.ManagerSaveException;
import service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHttpHandler extends BaseHttpHandler implements HttpHandler {

    public SubtasksHttpHandler(TaskManager tm) {
        super(tm);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                if (getLastPartOfPath(exchange).equals("subtasks") && getPartsOfPathNumber(exchange) == 2) {
                    handleGetALLSubtasks(exchange);
                    System.out.println("Запрос GET /subtasks обработан.");
                } else if (getPartsOfPathNumber(exchange) == 3) {
                    handleGetSubtaskById(exchange);
                    System.out.println("Запрос GET /subtasks/{id] обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            case "POST":
                if (getLastPartOfPath(exchange).equals("subtasks") && getPartsOfPathNumber(exchange) == 2) {
                    handlePostSubtask(exchange);
                    System.out.println("Запрос POST /subtasks обработан.");
                    break;
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
            case "DELETE":
                if (getPartsOfPathNumber(exchange) == 3) {
                    handleDeleteSubtask(exchange);
                    System.out.println("Запрос DELETE /subtasks/{id} обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            default:
                sendText(exchange, "Метод запроса не поддерживается сервером.", 405);
                System.out.println("Неизвестный метод.");
        }
    }

    private void handleGetALLSubtasks(HttpExchange exchange) throws IOException {
        final String response = gson.toJson(tm.getSubtasks());
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        sendText(exchange, response, 200);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        final Optional<Integer> subtaskIdOpt = getIdFromURL(exchange);
        if (subtaskIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id подзадачи", 400);
            return;
        }
        try {
            final Subtask subtask = tm.getSubtask(subtaskIdOpt.get());
            final String response = gson.toJson(subtask);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        final Subtask subtask = gson.fromJson(readText(exchange), Subtask.class);
        if (subtask.getId() == 0) {
            try {
                tm.addNewSubtask(subtask);
                sendText(exchange, "Новая подзадача сохранена.", 201);
            } catch (ManagerSaveException e) {
                sendText(exchange, e.getMessage(), 500);
            } catch (FoundIntersectionException e) {
                sendText(exchange, e.getMessage(), 406);
            }
        } else {
            try {
                tm.updateSubtask(subtask);
                sendText(exchange, "Подзадача с id " + subtask.getId() + " обновлена.", 201);
            } catch (ManagerSaveException e) {
                sendText(exchange, e.getMessage(), 500);
            } catch (NotFoundException e) {
                sendText(exchange, e.getMessage(), 404);
            } catch (FoundIntersectionException e) {
                sendText(exchange, e.getMessage(), 406);
            }
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        final Optional<Integer> subtaskIdOpt = getIdFromURL(exchange);
        if (subtaskIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id подзадачи", 400);
            return;
        }
        try {
            final Subtask subtask = tm.getSubtask(subtaskIdOpt.get());
            final String response = gson.toJson(subtask);
            tm.deleteSubtask(subtaskIdOpt.get());
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        } catch (ManagerSaveException e) {
            sendText(exchange, e.getMessage(), 500);
        }
    }
}
