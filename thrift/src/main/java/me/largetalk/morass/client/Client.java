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
    private long startTime, endTime;

    public void startClient() {
        startTime = System.nanoTime();

        TTransport transport;
        try {
            transport = new TSocket("localhost", 9090);
            transport.open();
            System.out.println("client .....");

            TProtocol protocol = new TBinaryProtocol(transport);
            Ernie.Client client = new Ernie.Client(protocol);

            for(int i=100; i>0; i--) {
                int randomInt = (int) (Math.random() * 10);
                Impression impl = new Impression(String.valueOf(randomInt) , "pageurl", "ip");
                LuckAd luckAd = client.bet(impl);
                System.out.println(luckAd.toString());
            }

            transport.close();
        } catch (TTransportException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        } catch (TException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        } finally {
            endTime = System.nanoTime();
            logger.info("client total time {} 微秒", (endTime - startTime)/1000);
        }
    }

    public static void main(String[] args) {
        logger.debug("begin thrift client");
        Client client = new Client();

        client.startClient();
    }
}
