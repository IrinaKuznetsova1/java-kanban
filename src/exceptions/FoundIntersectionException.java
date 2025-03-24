package exceptions;

public class FoundIntersectionException extends RuntimeException {
    public FoundIntersectionException() {
        super("Время выполнения пересекается с другими объектами.");
    }
}
