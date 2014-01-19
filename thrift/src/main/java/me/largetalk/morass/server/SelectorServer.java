package me.largetalk.morass.server;

import me.largetalk.morass.thrift.Ernie;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.Selector;

/**
 * Created by largetalk on 1/19/14.
 */
public class SelectorServer {
    private  static Logger logger = LoggerFactory.getLogger(Server.class);

    private static class InnerServer {
        public void start(){
            try {
                Ernie.Processor processor = new Ernie.Processor(new ErnieHandler());
                TNonblockingServerTransport transport = new TNonblockingServerSocket(9090);
                TCompactProtocol.Factory portFactory = new TCompactProtocol.Factory();

                TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(transport);
                args.processor(processor);
                args.protocolFactory(portFactory);
                args.workerThreads(10);
                args.selectorThreads(2);

                TServer server  = new TThreadedSelectorServer(args);
                server.serve();
            } catch (TTransportException ex) {

                ex.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        logger.debug("begin main server");
        //System.setProperty("log4j.configuration", "log4j.properties");

        System.out.println("server");
        //Server outServer = new Server();

        InnerServer server = new InnerServer();
        server.start();
    }
}
