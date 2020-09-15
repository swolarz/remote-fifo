package pl.put.swolarz.rfifo.protocol;

public class FifoSideAlreadyBoundException extends Exception {

    public enum FifoSide {
        READER, WRITER;
    }

    private final FifoSide fifoSide;

    public FifoSideAlreadyBoundException(FifoSide fifoSide) {
        super("Fifo client side already bound");
        this.fifoSide = fifoSide;
    }

    public FifoSide getFifoSide() {
        return fifoSide;
    }
}
