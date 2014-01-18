package me.largetalk.morass.client;

/**
 * Created by largetalk on 1/19/14.
 */
import me.largetalk.morass.thrift.Ernie;
import me.largetalk.morass.thrift.Impression;
import me.largetalk.morass.thrift.LuckAd;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncClient {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private long startTime, endTime;

    public void startClient() {
        startTime = System.nanoTime();

        TTransport transport;
        try {
            transport = new TFramedTransport(new TSocket("localhost", 9090, 300));

            System.out.println("client .....");

            TProtocol protocol = new TCompactProtocol(transport);
            Ernie.Client client = new Ernie.Client(protocol);
            transport.open();

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
        AsyncClient client = new AsyncClient();

        client.startClient();
    }
}
