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


public class Server {
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
        System.out.println("server");
        Server outServer = new Server();
        InnerServer server = outServer.new InnerServer();
        server.start();
    }
}
