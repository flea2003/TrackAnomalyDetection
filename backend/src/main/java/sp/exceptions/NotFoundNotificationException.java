package sp.exceptions;

public class NotFoundNotificationException extends Exception {
    /**
     * Default constructor which initiates the exception with a default message.
     */
    public NotFoundNotificationException() {
        super("Couldn't find such notification");
    }

    /**
     * Constructor which initiates the exception with a predefined message.
     *
     * @param msg message of the exception
     */
    public NotFoundNotificationException(String msg) {
        super(msg);
    }

}
