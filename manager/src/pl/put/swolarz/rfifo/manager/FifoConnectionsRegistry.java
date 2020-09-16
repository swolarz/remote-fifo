package pl.put.swolarz.rfifo.manager;

import pl.put.swolarz.rfifo.protocol.*;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static pl.put.swolarz.rfifo.protocol.FifoSideAlreadyBoundException.FifoSide;


class FifoConnectionsRegistry implements FifoRegistry {

    private final Map<String, FifoConnection> fifoConnections;

    public FifoConnectionsRegistry() {
        this.fifoConnections = new HashMap<>();
    }


    @Override
    public FifoWriter connectFifoReader(String path, FifoReader reader) throws FifoSideAlreadyBoundException {
        System.out.printf("Received reader connection to fifo: %s%n", path);
        validateFifoPath(path);

        if (reader == null)
            throw new IllegalArgumentException("Reader did not pass a reference to itself");

        return connectReader(path, reader);
    }

    private synchronized FifoWriter connectReader(String path, FifoReader reader) throws FifoSideAlreadyBoundException {
        FifoConnection fifo = getOrCreateConnection(path);

        if (fifo.getReader() != null && heartbeatReader(path, fifo.getReader()))
            throw new FifoSideAlreadyBoundException(FifoSide.READER);

        fifo.connectReader(reader);

        return fifo.getWriter();
    }

    @Override
    public FifoReader connectFifoWriter(String path, FifoWriter writer) throws FifoSideAlreadyBoundException {
        System.out.printf("Received writer connection to fifo: %s%n", path);
        validateFifoPath(path);

        if (writer == null)
            throw new IllegalArgumentException("Writer did not pass a reference to itself");

        return connectWriter(path, writer);
    }

    private synchronized FifoReader connectWriter(String path, FifoWriter writer) throws FifoSideAlreadyBoundException {
        FifoConnection fifo = getOrCreateConnection(path);

        if (fifo.getWriter() != null && heartbeatWriter(path, fifo.getWriter()))
            throw new FifoSideAlreadyBoundException(FifoSide.WRITER);

        fifo.connectWriter(writer);

        return fifo.getReader();
    }

    private synchronized FifoConnection getOrCreateConnection(String fifoPath) {
        if (!fifoConnections.containsKey(fifoPath))
            fifoConnections.put(fifoPath, new FifoConnection());

        FifoConnection connection = fifoConnections.get(fifoPath);

        if (connection.isConnected()
                && !(heartbeatReader(fifoPath, connection.getReader()) && heartbeatWriter(fifoPath, connection.getWriter()))) {

            System.err.printf("Resetting fifo connection: %s%n", fifoPath);

            connection = new FifoConnection();
            fifoConnections.put(fifoPath, connection);
        }

        return connection;
    }

    private boolean heartbeatReader(String fifoPath, FifoReader reader) {
        System.out.printf("Sending heartbeat to reader at: '%s'... ", fifoPath);
        boolean pong = heartbeatClient(reader);

        System.out.println(pong ? "Ok." : "Not responding.");

        return pong;
    }

    private boolean heartbeatWriter(String fifoPath, FifoWriter writer) {
        System.out.printf("Sending heartbeat to writer at: '%s'... ", fifoPath);
        boolean pong = heartbeatClient(writer);

        System.out.println(pong ? "Ok." : "Not responding.");

        return pong;
    }

    private boolean heartbeatClient(FifoClient client) {
        try {
            return client.ping();
        }
        catch (RemoteException e) {
            return false;
        }
    }

    private void validateFifoPath(String path) {
        if (path == null || path.isBlank())
            throw new IllegalArgumentException(String.format("Invalid named pipe value: %s", path));
    }


    private static class FifoConnection {
        private FifoReader reader;
        private FifoWriter writer;

        public void connectReader(FifoReader reader) {
            this.reader = reader;
        }

        public void connectWriter(FifoWriter writer) {
            this.writer = writer;
        }

        public FifoReader getReader() {
            return reader;
        }

        public FifoWriter getWriter() {
            return writer;
        }

        public boolean isConnected() {
            return (reader != null && writer != null);
        }
    }
}
