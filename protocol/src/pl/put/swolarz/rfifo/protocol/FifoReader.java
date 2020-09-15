package pl.put.swolarz.rfifo.protocol;

import java.rmi.RemoteException;


public interface FifoReader extends FifoClient {
    void accept(byte[] bytes, FifoWriter from) throws RemoteException, ConsumerFailureException;
}
