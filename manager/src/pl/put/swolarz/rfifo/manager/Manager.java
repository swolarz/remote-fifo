package pl.put.swolarz.rfifo.manager;

import org.apache.commons.cli.*;
import pl.put.swolarz.rfifo.protocol.FifoRegistry;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Manager {

    private static Registry registry;


    public static void main(String[] args) {
        Options options = new Options();

        Option portOption = new Option("p", "port", true, "RMI registry port");
        portOption.setRequired(false);
        portOption.setType(Integer.class);

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

            System.exit(2);
        }

        int port = Integer.parseInt(cmd.getOptionValue(portOption.getOpt(), "1099"));
        if (port > 65535) {
            System.out.printf("Error: invalid port value = %d%n", port);
            System.exit(2);
        }

        try {
            setupManager(port);
        }
        catch (RemoteException e) {
            System.out.printf("Error: failed to setup fifo registry: %s%n", e.getMessage());
            System.exit(1);
        }
    }

    private static void setupManager(int port) throws RemoteException {
        registry = LocateRegistry.createRegistry(port);

        FifoConnectionsRegistry registrar = new FifoConnectionsRegistry();
        FifoRegistry fifoRegistry = (FifoRegistry) UnicastRemoteObject.exportObject(registrar, 0);

        registry.rebind(FifoRegistry.REGISTRY_KEY, fifoRegistry);
    }
}