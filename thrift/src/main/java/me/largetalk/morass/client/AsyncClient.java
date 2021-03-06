package me.largetalk.morass.client;

import me.largetalk.morass.thrift.Ernie;
import me.largetalk.morass.thrift.Impression;
import me.largetalk.morass.thrift.LuckAd;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by largetalk on 1/19/14.
 */
public class AsyncClient {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private long startTime, endTime;

    public void startClient() {
        startTime = System.nanoTime();

        try {

            System.out.println("client .....");

            Ernie.AsyncClient client = new Ernie.AsyncClient(new TCompactProtocol.Factory(),
                    new TAsyncClientManager(),
                    new TNonblockingSocket("localhost", 9090));


            for(int i=100; i>0; i--) {
                int randomInt = (int) (Math.random() * 10);
                Impression impl = new Impression(String.valueOf(randomInt) , "pageurl", "ip");
                client.bet(impl, new BetMethodCallback());

            }

        } catch (TTransportException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        } catch (TException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            endTime = System.nanoTime();
            logger.info("client total time {} 微秒", (endTime - startTime)/1000);
        }
    }

    public static void main(String[] args) {

        logger.debug("begin thrift client");
        NBClient client = new NBClient();

        client.startClient();
    }

    class BetMethodCallback implements AsyncMethodCallback<Ernie.AsyncClient.bet_call> {
        public void onComplete(Ernie.AsyncClient.bet_call bet_call) {
            try {
                LuckAd luckAd = bet_call.getResult();
                System.out.println(luckAd.toString());
            } catch (TException ex) {
                ex.printStackTrace();
            }

        }

        public void onError(Exception ex) {
            ex.printStackTrace();
        }
    }
}
