package pl.put.swolarz.rfifo.producer;

import pl.put.swolarz.rfifo.protocol.ConsumerFailureException;
import pl.put.swolarz.rfifo.protocol.FifoReader;
import pl.put.swolarz.rfifo.protocol.FifoWriter;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

class ProducerWriter implements FifoWriter {
    private static final int BUFFER_SIZE = 4096;

    private final InputStream input;

    private FifoReader consumer;
    private FifoWriter selfStub;

    private boolean writing;


    public ProducerWriter(InputStream input) {
        this.input = input;
        this.writing = false;
    }

    @Override
    public synchronized void notifyReady(FifoReader reader) {
        if (selfStub == null)
            throw new RuntimeException("Self stub not initialized");

        if (writing)
            return;

        this.consumer = reader;
        CompletableFuture.runAsync(this::writeToFifo);
    }

    private synchronized void writeToFifo() {
        try {
            try {
                byte[] bytes;
                do {
                    bytes = input.readNBytes(BUFFER_SIZE);
                    consumer.accept(bytes, selfStub);

                } while (bytes.length > 0);
            }
            catch (IOException e) {
                System.err.printf("Error: failed to read input stream: %s%n", e.getMessage());
            }
            catch (ConsumerFailureException e) {
                System.err.printf("Warning: reader side failure: %s%n", e.getMessage());
            }

            // Send EOF
            consumer.accept(null, selfStub);
        }
        catch (RemoteException | ConsumerFailureException e) {
            System.err.printf("Error: broken pipe: %s%n", e.getMessage());
        }

        finalizeFifoWrite();
    }

    private void finalizeFifoWrite() {
        this.writing = false;
    }

    @Override
    public boolean ping() {
        return writing;
    }

    public void setupRemoteConnections(FifoWriter selfStub) {
        this.selfStub = selfStub;
    }
}
