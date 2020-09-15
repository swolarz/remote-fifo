package pl.put.swolarz.rfifo.protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface FifoClient extends Remote {
    boolean ping() throws RemoteException;
}
