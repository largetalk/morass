package me.largetalk.http.core;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;

public final class HttpCraft {
    
     public static String getUaFromHeader(HttpRequest httpRequest) {
        Header uaHeader;
        uaHeader = httpRequest.getFirstHeader("User-Agent");
        if (uaHeader != null) {
            return uaHeader.getValue();
        }
        
        uaHeader = httpRequest.getFirstHeader("user-agent");
        if (uaHeader != null) {
            return uaHeader.getValue();
        }
        return null;
    }

    public static String getIPFromHeader(HttpRequest httpRequest) {
        Header ipHeader;
        ipHeader = httpRequest.getFirstHeader("X-Forwarded-For");
        if (ipHeader != null) {
            return ipHeader.getValue();
        }
        return null;
    }
    
    public static void main(String args[]) {
        HttpGet get = new HttpGet("http://www.xxx.com");
        get.addHeader("user-agent", "os123");
        System.out.println(getUaFromHeader(get));
    }
}
