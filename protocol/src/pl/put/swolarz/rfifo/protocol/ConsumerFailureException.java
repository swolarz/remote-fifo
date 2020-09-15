package pl.put.swolarz.rfifo.protocol;

public class ConsumerFailureException extends Exception {

    public ConsumerFailureException(String message) {
        super(message);
    }
}
