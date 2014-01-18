package me.largetalk.morass.client;

import me.largetalk.morass.thrift.Ernie;
import me.largetalk.morass.thrift.Impression;
import me.largetalk.morass.thrift.LuckAd;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by largetalk on 1/18/14.
 */
public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    public void startClient() {
        TTransport transport;
        try {
            transport = new TSocket("localhost", 9090);
            transport.open();
            System.out.println("client .....");

            TProtocol protocol = new TBinaryProtocol(transport);
            Ernie.Client client = new Ernie.Client(protocol);
            Impression impl = new Impression("asid", "pageurl", "ip");
            LuckAd luckAd = client.bet(impl);
            System.out.println(luckAd.toString());

            transport.close();
        } catch (TTransportException ex) {
            ex.printStackTrace();
        } catch (TException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        logger.debug("begin thrift client");
        Client client = new Client();

        client.startClient();
    }
}
