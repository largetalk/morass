package me.largetalk.morass.server;

/**
 * Created by largetalk on 1/19/14.
 */

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.largetalk.morass.thrift.Ernie.Processor;

public class AsyncServer {
    private  static Logger logger = LoggerFactory.getLogger(Server.class);

    private class InnerServer {
        public void start(){
            try {
                Processor processor = new Processor(new ErnieHandler());
                TNonblockingServerTransport transport = new TNonblockingServerSocket(9090);


                THsHaServer.Args args = new THsHaServer.Args(transport);
                args.protocolFactory(new TCompactProtocol.Factory());
                args.transportFactory(new TFramedTransport.Factory());
                args.processorFactory(new TProcessorFactory(processor));

                TServer server  = new THsHaServer(args);
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
        AsyncServer outServer = new AsyncServer();

        InnerServer server = outServer.new InnerServer();
        server.start();
    }
}
