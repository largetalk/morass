/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.http.api.json;

import com.adsame.rtb.lib.jackson.JacksonTranslator;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class Test {

    public static void main(String args[]) throws IOException {
        String json = "{\n"
                + "    \"method\" : \"requestid123\",\n"
                + "    \"time\" : \"request123\",\n"
                + "    \"params\" : {\n"
                + "        \"id\" : \"impid1230\",\n"
                + "        \"tagid\" : \"portal_01\",\n"
                + "        \"bidfloor\" : 0.1000000014901161\n"
                + "    }}";

        Request bidRequest = JacksonTranslator.jsonToObject(json,
                Request.class);
        System.out.println(bidRequest.getMethod());
        System.out.println(bidRequest.getTime());
        Iterator<Entry<String, Object>> it = bidRequest.getParams().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> entry = it.next();
            System.out.println(entry.getKey());
            System.out.println(entry.getValue().getClass());
        }
    }
}
