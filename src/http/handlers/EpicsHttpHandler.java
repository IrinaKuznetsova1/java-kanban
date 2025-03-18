package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import model.Epic;
import exceptions.ManagerSaveException;
import service.TaskManager;

import java.io.IOException;
import java.util.Optional;

public class EpicsHttpHandler extends BaseHttpHandler implements HttpHandler {

    public EpicsHttpHandler(TaskManager tm) {
        super(tm);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        switch (exchange.getRequestMethod()) {
            case "GET":
                if (getLastPartOfPath(exchange).equals("epics") && getPartsOfPathNumber(exchange) == 2) {
                    handleGetALLEpics(exchange);
                    System.out.println("Запрос GET /epics обработан.");
                    return;
                } else if (getLastPartOfPath(exchange).equals("subtasks") && getPartsOfPathNumber(exchange) == 4) {
                    handleGetEpicSubtasks(exchange);
                    System.out.println("Запрос GET /epics/{id]/subtasks обработан.");
                } else if (getPartsOfPathNumber(exchange) == 3) {
                    handleGetEpicById(exchange);
                    System.out.println("Запрос GET /epics/{id] обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            case "POST":
                if (getLastPartOfPath(exchange).equals("epics") && getPartsOfPathNumber(exchange) == 2) {
                    handlePostEpic(exchange);
                    System.out.println("Запрос POST /epics обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            case "DELETE":
                if (getPartsOfPathNumber(exchange) == 3) {
                    handleDeleteEpic(exchange);
                    System.out.println("Запрос DELETE /epics/{id} обработан.");
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            default:
                sendText(exchange, "Метод запроса не поддерживается сервером.", 405);
                System.out.println("Неизвестный метод.");
        }
    }

    private void handleGetALLEpics(HttpExchange exchange) throws IOException {
        final String response = gson.toJson(tm.getEpics());
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        sendText(exchange, response, 200);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        final Optional<Integer> epicIdOpt = getIdFromURL(exchange);
        if (epicIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id эпика", 400);
            return;
        }
        try {
            final Epic epic = tm.getEpic(epicIdOpt.get());
            final String response = gson.toJson(epic);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        final Optional<Integer> epicIdOpt = getIdFromURL(exchange);
        if (epicIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id эпика", 400);
            return;
        }
        try {
            final String response = gson.toJson(tm.getEpicSubtasks(epicIdOpt.get()));
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        final Epic epicJson = gson.fromJson(readText(exchange), Epic.class);
        if (epicJson.getId() == 0) {
            try {
                final Epic epic = new Epic(epicJson.getTitle(), epicJson.getDescription());
                tm.addNewEpic(epic);
                sendText(exchange, "Новый эпик сохранен.", 201);
            } catch (ManagerSaveException e) {
                sendText(exchange, e.getMessage(), 500);
            }
        } else {
            try {
                tm.updateEpic(epicJson);
                sendText(exchange, "Эпик с id " + epicJson.getId() + " обновлен.", 201);
            } catch (ManagerSaveException e) {
                sendText(exchange, e.getMessage(), 500);
            } catch (NotFoundException e) {
                sendText(exchange, e.getMessage(), 404);
            }
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        final Optional<Integer> epicIdOpt = getIdFromURL(exchange);
        if (epicIdOpt.isEmpty()) {
            sendText(exchange, "Некорректный id эпика", 400);
            return;
        }
        try {
            final Epic epic = tm.getEpic(epicIdOpt.get());
            final String response = gson.toJson(epic);
            tm.deleteEpic(epicIdOpt.get());
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            sendText(exchange, response, 200);
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        } catch (ManagerSaveException e) {
            sendText(exchange, e.getMessage(), 500);
        }
    }
}
