package exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Объект не найден.");
    }
}
