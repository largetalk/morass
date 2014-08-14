/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package me.largetalk.qdigest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class TestReplace {

    public static final String CLICK_URL_MACRO = "%%CLICK_URL%%";

    public static final String AUDIT_MESSAGE_MACRO = "${PARAMETER}";
    public static final String DSP_CLICK_SERVER_MACRO = "${DSPCLICKURL}";

    public static void main(String args[]) throws UnsupportedEncodingException {
        String clickServerURLTemplate = "http://adsame.com/?p=${PARAMETER}&u=${DSPCLICKURL}";

        String click_through_url = "http://sammix.adsame.com/c?z=sammix&la=0&si=1868&cg=7418&c=8724&ci=4745&or=11644&l=69036&bg=69651&b=64049&_ext=${CONTEXT_SHOW_CLICK}&u=http://www.visionkids.com.cn/about.php";

        String htmlSinppet = "<iframe width=\"300\" height=\"250\" frameborder=\"0\" scrolling=\"no\" marginwidth=\"0\" marginheight=\"0\" src=\"http://sammix.adsame.com/s?z=sammix&c=8724&l=69036&op=1&_ext=eJyTZu9gMjNnZWCONoplNORgYEtMKU7MTWUys-Bh4HLzDAoOiQ_zdA1nNFdkkE9KKtarzM9O1EvOz9UvyEzWNTUyNDIwNNc118soyc1hMjXkZGA3NjCIMDI1YDI1kmeQhRgWD1RkYmBmbGAIhAZGpkYGJmbGICUGHAwMjG6vOp02MgAAUQYfcA&wurl=http%3A%2F%2Fsammix.adsame.com%3A9862%2F&wkeys=ext|price|bid&ext=YWRzYW1lX3VpZD0zMWUyMjgzMTQyZjk2YyZzb2x1dGlvbl9pZD02OTAzNg&price=%%PRICE%%&bid=%%ID%%&dp=%%CLICK_URL%%\"></iframe><iframe src=\"http://s.cr-nielsen.com/hat?_t=i&_htsinfo=SSYyJjgwMDAwMTMyJjEwMDAzODc1JjMwMDU5NTcxJvAB\" width=\"0\" height=\"0\" frameborder=\"0\" scrolling=\"no\" marginwidth=\"0\" marginheight=\"0\"></iframe>";

        String clickURLForUsToTrack =
                clickServerURLTemplate.replace(AUDIT_MESSAGE_MACRO,
                        "ABCDEFG");
        clickURLForUsToTrack =
                clickURLForUsToTrack.replace(DSP_CLICK_SERVER_MACRO,
                        generateDspClickUrl(click_through_url));
        clickURLForUsToTrack = escapeToJSCompatibleString(clickURLForUsToTrack);

        System.out.println(clickURLForUsToTrack);
        System.out.println("##########################################");

        String renderedHTML = htmlSinppet.replace(CLICK_URL_MACRO,
                clickURLForUsToTrack);

        System.out.println(renderedHTML);
        testForList();
    }

    private static String generateDspClickUrl(String clickurl) {

        try {
            return URLEncoder.encode(clickurl, "UTF-8");
        } catch (UnsupportedEncodingException ex) {

        }

        return "";
    }

    private static String escapeToJSCompatibleString(String source)
            throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(source, "UTF-8");

        // this is because java's urlEncode encode space to +
        // while javascript's counter parts encode space to %20
        // we need to make it compatible
        return encoded.replace("+", "%20");
    }
    
    private static void testForList() {
        List<Long> solutionList = new ArrayList<Long>();
        solutionList.add(1L);
        solutionList.add(2L);
        solutionList.add(3L);
        solutionList.add(4L);
        for (Long i: solutionList) {
            System.out.println(i);
        }
    }

}
