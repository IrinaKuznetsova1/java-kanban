package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;

import java.io.IOException;

public class HistoryHttpHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHttpHandler(TaskManager tm) {
        super(tm);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                if (getLastPartOfPath(exchange).equals("history") && getPartsOfPathNumber(exchange) == 2) {
                    final String response = gson.toJson(tm.getHistory());
                    exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
                    sendText(exchange, response, 200);
                } else {
                    sendText(exchange, "Неизвестный запрос.", 400);
                }
                break;
            default:
                sendText(exchange, "Метод запроса не поддерживается сервером.", 405);
                System.out.println("Неизвестный метод.");
        }
    }
}
