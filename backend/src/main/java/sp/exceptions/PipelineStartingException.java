package sp.exceptions;

public class PipelineStartingException extends Exception {
    /**
    * Default constructor which initializes the exception with a default message.
    */
    public PipelineStartingException() {
        super("The pipeline has not yet initialized");
    }

    /**
    * Constructor which initializes the exception with a predefined message.
    *
    * @param message message
    */
    public PipelineStartingException(String message) {
        super(message);
    }
}
