package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.ManagerSaveException;
import http.HttpTaskServer;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

abstract class BaseHttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final Gson gson = HttpTaskServer.getGson();
    protected final TaskManager tm;

    BaseHttpHandler(TaskManager tm) {
        this.tm = tm;
    }

    protected void sendText(HttpExchange exchange, String text, int code) throws IOException {
        final byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        final OutputStream os = exchange.getResponseBody();
        exchange.sendResponseHeaders(code, resp.length);
        os.write(resp);
        os.close();
    }

    protected String getLastPartOfPath(HttpExchange exchange) {
        final String requestPath = exchange.getRequestURI().getPath();
        final String[] partsOfPath = requestPath.split("/");
        return partsOfPath[partsOfPath.length - 1];
    }

    protected int getPartsOfPathNumber(HttpExchange exchange) {
        final String requestPath = exchange.getRequestURI().getPath();
        return requestPath.split("/").length;
    }

    protected Optional<Integer> getIdFromURL(HttpExchange exchange) {
        final String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    protected String readText(HttpExchange exchange) {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка обработки запроса");
        }
    }
}
