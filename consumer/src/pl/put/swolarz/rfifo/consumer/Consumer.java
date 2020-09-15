package pl.put.swolarz.rfifo.consumer;

import org.apache.commons.cli.*;
import pl.put.swolarz.rfifo.protocol.FifoReader;
import pl.put.swolarz.rfifo.protocol.FifoRegistry;
import pl.put.swolarz.rfifo.protocol.FifoSideAlreadyBoundException;
import pl.put.swolarz.rfifo.protocol.FifoWriter;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Consumer {
    public static void main(String[] args) {
        Options options = new Options();

        Option fifoOption = new Option("f", "fifo", true, "Named pipe name/path");
        fifoOption.setRequired(true);
        fifoOption.setType(String.class);

        Option hostOption = new Option("h", "host", true, "RMI registry host");
        hostOption.setRequired(false);
        hostOption.setType(String.class);

        Option portOption = new Option("p", "port", true, "RMI registry port");
        portOption.setRequired(false);
        portOption.setType(Integer.class);

        options.addOption(fifoOption);
        options.addOption(hostOption);
        options.addOption(portOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String fifoPath = cmd.getOptionValue(fifoOption.getOpt());
        String host = cmd.getOptionValue(hostOption.getOpt(), "localhost");
        int port = Integer.parseInt(cmd.getOptionValue(portOption.getOpt(), "1099"));

        FifoRegistry fifoRegistry = resolveFifoRegistry(host, port);
        connectReader(fifoPath, fifoRegistry);
    }

    private static FifoRegistry resolveFifoRegistry(String host, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (FifoRegistry) registry.lookup(FifoRegistry.REGISTRY_KEY);
        }
        catch (RemoteException | NotBoundException e) {
            System.err.printf("Error: Failed to connect to fifo registry: %s%n", e.getMessage());
            System.exit(2);
        }

        return null;
    }

    private static void connectReader(String fifoPath, FifoRegistry fifoRegistry) {
        ConsumerReader reader = new ConsumerReader();

        try {
            FifoReader fifoReader = (FifoReader) UnicastRemoteObject.exportObject(reader, 0);
            reader.setupRemoteConnection(fifoReader);

            FifoWriter writer = fifoRegistry.connectFifoReader(fifoPath, fifoReader);

            if (writer != null)
                reader.acknowledgeProducer(writer);
        }
        catch (RemoteException e) {
            System.err.printf("Error: Failed to register reader for '%s' fifo: %s%n", fifoPath, e.getMessage());
            System.exit(1);
        }
        catch (FifoSideAlreadyBoundException e) {
            System.err.printf("Error: Fifo '%s' already in use by another reader: %s%n", fifoPath, e.getMessage());
            System.exit(2);
        }
    }
}
