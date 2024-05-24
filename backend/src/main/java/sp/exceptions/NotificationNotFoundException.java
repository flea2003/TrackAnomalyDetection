package sp.exceptions;

public class NotificationNotFoundException extends Exception {
    /**
     * Default constructor which initiates the exception with a default message.
     */
    public NotificationNotFoundException() {
        super("Couldn't find such notification");
    }

    /**
     * Constructor which initiates the exception with a predefined message.
     *
     * @param msg message of the exception
     */
    public NotificationNotFoundException(String msg) {
        super(msg);
    }

}
