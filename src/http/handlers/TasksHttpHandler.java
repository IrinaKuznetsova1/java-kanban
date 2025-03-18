package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.FoundIntersectionException;
import exceptions.NotFoundException;
import model.Task;
import exceptions.ManagerSaveException;
import service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class TasksHttpHandler extends BaseHttpHandler implements HttpHandler {

    public TasksHttpHandler(TaskManager tm) {
        super(tm);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                if (getLastPartOfPath(exchange).equals("tasks") && getPartsOfPathNumber(exchange) == 2) {
                    handleGetALLTasks(exchange);
                    System.out.println("Запрос GET /tasks обработан.");
                    return;
                } else if (getPartsOfPathNumber(exchange) == 3) {
                    handleGetTaskById(exchange);
                    System.out.println("Запрос GET /tasks/{id] обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            case "POST":
                if (getLastPartOfPath(exchange).equals("tasks") && getPartsOfPathNumber(exchange) == 2) {
                    handlePostTask(exchange);
                    System.out.println("Запрос POST /tasks обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            case "DELETE":
                if (getPartsOfPathNumber(exchange) == 3) {
                    handleDeleteTask(exchange);
                    System.out.println("Запрос DELETE /tasks/{id} обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            default:
                sendText(exchange, "Метод запроса не поддерживается сервером.", 405);
                System.out.println("Неизвестный метод.");
        }
    }

    private void handleGetALLTasks(HttpExchange exchange) throws IOException {
        final String response = gson.toJson(tm.getTasks());
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        sendText(exchange, response, 200);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        final Optional<Integer> taskIdOpt = getIdFromURL(exchange);
        if (taskIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id задачи", 400);
            return;
        }
        try {
            final Task task = tm.getTask(taskIdOpt.get());
            final String response = gson.toJson(task);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        final Task task = gson.fromJson(readText(exchange), Task.class);
        if (task.getId() == 0) {
            try {
                tm.addNewTask(task);
                sendText(exchange, "Новая задача сохранена.", 201);
            } catch (ManagerSaveException e) {
                sendText(exchange, e.getMessage(), 500);
            } catch (FoundIntersectionException e) {
                sendText(exchange, e.getMessage(), 406);
            }
        } else {
            try {
                tm.updateTask(task);
                sendText(exchange, "Задача с id " + task.getId() + " обновлена.", 201);
            } catch (ManagerSaveException e) {
                sendText(exchange, e.getMessage(), 500);
            } catch (NotFoundException e) {
                sendText(exchange, e.getMessage(), 404);
            } catch (FoundIntersectionException e) {
                sendText(exchange, e.getMessage(), 406);
            }
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        final Optional<Integer> taskIdOpt = getIdFromURL(exchange);
        if (taskIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id задачи", 400);
            return;
        }
        try {
            final Task task = tm.getTask(taskIdOpt.get());
            final String response = gson.toJson(task);
            tm.deleteTask(taskIdOpt.get());
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        } catch (ManagerSaveException e) {
            sendText(exchange, e.getMessage(), 500);
        }
    }
}
