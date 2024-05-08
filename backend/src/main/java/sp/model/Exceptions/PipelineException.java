package sp.model.Exceptions;

public class PipelineException extends Exception{

    public PipelineException(){
        super("The Kafka server threw an Exception");
    }

    public PipelineException(String message){
        super(message);
    }

}
