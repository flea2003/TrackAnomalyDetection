package sp.model.Exceptions;

public class NotExistingShipException  extends Exception{

    public NotExistingShipException(){
        super("Couldn't find such ship.");
    }

    public NotExistingShipException(String msg){
        super(msg);
    }

}
