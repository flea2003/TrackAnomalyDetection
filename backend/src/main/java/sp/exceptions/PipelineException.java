package sp.exceptions;

public class PipelineException extends Exception {

    /**
     * Default constructor which initializes the exception with a default message.
     */
    public PipelineException() {
        super("The Kafka server threw an Exception");
    }

    /**
     * Constructor which initializes the exception with a predefined message.
     *
     * @param message message
     */
    public PipelineException(String message) {
        super(message);
    }

}
