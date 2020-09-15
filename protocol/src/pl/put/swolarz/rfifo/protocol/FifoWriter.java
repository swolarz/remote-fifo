package pl.put.swolarz.rfifo.protocol;

import java.rmi.RemoteException;


public interface FifoWriter extends FifoClient {
    void notifyReady(FifoReader reader) throws RemoteException;
}
