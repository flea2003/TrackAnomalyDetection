package sp.exceptions;

public class NotExistingShipException  extends Exception{

    /**
     * Default constructor which initiates the exception with a default message.
     */
    public NotExistingShipException(){
        super("Couldn't find such ship.");
    }

    /**
     * Constructor which initiates the exception with a predefined message
     * @param msg
     */
    public NotExistingShipException(String msg){
        super(msg);
    }

}
