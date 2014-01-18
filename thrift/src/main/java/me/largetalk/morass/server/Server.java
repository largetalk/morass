package me.largetalk.morass.server;

/**
 * Created by largetalk on 1/18/14.
 */
import me.largetalk.morass.thrift.Ernie.Processor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Server {
    private  static Logger logger = LoggerFactory.getLogger(Server.class);

    private class InnerServer {
        public void start(){
            try {
                Processor processor = new Processor(new ErnieHandler());
                TServerTransport transport = new TServerSocket(9090);
                Factory portFactory = new TBinaryProtocol.Factory(true, true);

                TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport);
                args.processor(processor);
                args.protocolFactory(portFactory);

                TServer server  = new TThreadPoolServer(args);
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
        Server outServer = new Server();

        InnerServer server = outServer.new InnerServer();
        server.start();
    }
}
