package me.largetalk.mograss.server;

/**
 * Created by largetalk on 1/18/14.
 */
import me.largetalk.mograss.thrift.Ernie;
import me.largetalk.mograss.thrift.Impression;
import me.largetalk.mograss.thrift.LuckAd;

public class ErnieHandler implements Ernie.Iface {
    public LuckAd bet(Impression impl) {
        System.out.println(" Ernie handler " + impl.toString());
        return new LuckAd("fake bid", "hello world");
    }
}
