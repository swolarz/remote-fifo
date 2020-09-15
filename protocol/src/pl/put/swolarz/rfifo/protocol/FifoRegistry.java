package pl.put.swolarz.rfifo.protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FifoRegistry extends Remote {
    String REGISTRY_KEY = "named-pipe-registry";

    FifoWriter connectFifoReader(String path, FifoReader reader) throws RemoteException, FifoSideAlreadyBoundException;
    FifoReader connectFifoWriter(String path, FifoWriter writer) throws RemoteException, FifoSideAlreadyBoundException;
}
