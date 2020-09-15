package pl.put.swolarz.rfifo.protocol;


public interface FifoWriter extends FifoClient {
    void notifyReady(FifoReader reader);
}
