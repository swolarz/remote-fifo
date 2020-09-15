package pl.put.swolarz.rfifo.consumer;

import pl.put.swolarz.rfifo.protocol.ConsumerFailureException;
import pl.put.swolarz.rfifo.protocol.FifoReader;
import pl.put.swolarz.rfifo.protocol.FifoWriter;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ConsumerReader implements FifoReader {

    private FifoWriter producer;
    private FifoReader selfStub;

    private boolean awaiting;
    private final ScheduledThreadPoolExecutor heartbeatExecutor;


    public ConsumerReader() {
        this.heartbeatExecutor = new ScheduledThreadPoolExecutor(1);
        this.awaiting = true;
    }

    @Override
    public synchronized void accept(byte[] bytes, FifoWriter from) throws ConsumerFailureException {
        if (!awaiting)
            return;

        acknowledgeProducer(from);

        if (bytes == null) {
            CompletableFuture.runAsync(this::close);
            return;
        }

        try {
            consumeBytes(bytes);
        }
        catch (IOException e) {
            System.err.printf("Error: failed to write received bytes to standard out: %s%n", e.getMessage());
            throw new ConsumerFailureException("Failed to process received bytes");
        }
    }

    public void acknowledgeProducer(FifoWriter producer) {
        if (this.producer != null)
            return;

        this.producer = producer;

        heartbeatExecutor.scheduleWithFixedDelay(this::sendHeartbeat, 1, 5, TimeUnit.SECONDS);
        CompletableFuture.runAsync(this::notifyProducer);
    }

    private void notifyProducer() {
        try {
            producer.notifyReady(selfStub);
        }
        catch (RemoteException e) {
            System.err.printf("Error: failed to reach producer due to connection error: %s%n", e.getMessage());
            shutdown();
        }
    }

    private void sendHeartbeat() {
        try {
            boolean pong = producer.ping();
            if (!pong)
                shutdown();
        }
        catch (RemoteException e) {
            System.err.printf("Warning: fifo writer side stopped responding: %s%n", e.getMessage());
            shutdown();
        }
    }

    private void consumeBytes(byte[] bytes) throws IOException {
        System.out.write(bytes);
    }

    private void shutdown() {
        CompletableFuture.runAsync(this::close);
    }

    private synchronized void close() {
        this.awaiting = false;
        heartbeatExecutor.shutdown();

        try {
            heartbeatExecutor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException ignored) {
        }

        System.exit(0);
    }

    @Override
    public boolean ping() {
        return awaiting;
    }

    public void setupRemoteConnection(FifoReader selfStub) {
        this.selfStub = selfStub;
    }
}
