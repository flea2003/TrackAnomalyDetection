package sp.exceptions;

public class DatabaseException extends Exception {
    /**
     * Default constructor which initializes the exception with a default message.
     */
    public DatabaseException() {
        super("The database query failed.");
    }

    /**
     * Constructor which initializes the exception with a predefined message.
     *
     * @param message message
     */
    public DatabaseException(String message) {
        super(message);
    }
}
