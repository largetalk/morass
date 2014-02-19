/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HttpHelper.class);

    // The default implementation of the BasicHeaderValueParser
    // treats both ';' and ',' as delimiters.
    // However, in our cookie, values might contain ','.
    // As a result, we should extend the BasicHeaderValueParser,
    // making ';' as the only delimiter.
    // In addition, the BasicHeaderValueParser is Immutable,
    // So, we can share single instance in multi-thread environment.
    private static final BasicHeaderValueParser PARSER
            = new BasicHeaderValueParser() {

        @Override
        public NameValuePair parseNameValuePair(
                final CharArrayBuffer buffer,
                final ParserCursor cursor) {
            return parseNameValuePair(buffer, cursor,
                    new char[]{';'});
        }
    };

    public static String excludeParameters(HttpRequest request,
            HashSet<String> keySet) {
        StringBuilder stringBuilder = new StringBuilder();
        RequestLine requestLine = request.getRequestLine();
        URI uri;
        try {
            uri = new URI(requestLine.getUri());
        } catch (URISyntaxException ex) {
            LOGGER.warn("invalid uri syntax", ex);
            return "";
        }
        List<NameValuePair> pairList = URLEncodedUtils.parse(uri, "UTF-8");
        boolean isFirstElement = true;
        for (NameValuePair pair : pairList) {
            String tempName = pair.getName();
            String tempValue = pair.getValue();
            tempValue = (tempValue == null) ? "" : tempValue;
            if (!keySet.contains(tempName)) {
                if (isFirstElement) {
                    isFirstElement = false;
                } else {
                    stringBuilder.append("&");
                }
                stringBuilder.append(tempName);
                stringBuilder.append("=");
                stringBuilder.append(tempValue);
            }
        }
        return stringBuilder.toString();
    }

    public static HashMap<String, String> extractParameters(
            HttpRequest request, HashSet<String> keySet) {
        HashMap<String, String> map = new HashMap<String, String>();
        RequestLine requestLine = request.getRequestLine();
        URI uri;
        try {
            uri = new URI(requestLine.getUri());
        } catch (URISyntaxException ex) {
            LOGGER.warn("invalid uri syntax", ex);
            return map;
        }
        List<NameValuePair> pairList = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair pair : pairList) {
            String tempName = pair.getName();
            String tempValue = pair.getValue();
            tempValue = (tempValue == null) ? "" : tempValue;
            if (keySet.contains(tempName) && !map.containsKey(tempName)) {
                map.put(tempName, tempValue);
            }
        }
        return map;
    }

    public static String extractCookie(HttpRequest request, String key) {
        String value = null;
        HeaderIterator it = request.headerIterator("Cookie");
        while (it.hasNext() && !isConcretePresent(value)) {
            Header header = it.nextHeader();
            String paramString = header.getValue();
            NameValuePair pairArray[] =
                    BasicHeaderValueParser.parseParameters(
                    paramString, PARSER);
            for (NameValuePair pair : pairArray) {
                String tempKey = pair.getName();
                String tempValue = pair.getValue();
                tempValue = (tempValue == null) ? "" : tempValue;
                if (tempKey.equals(key)) {
                    value = tempValue;
                    if (isConcretePresent(value)) {
                        break;
                    }
                }
            }
        }
        return value;
    }

    public static boolean isConcretePresent(String value) {
        return value != null && !value.isEmpty();
    }

    public static boolean isEmptyPresent(String value) {
        return value != null && value.isEmpty();
    }

    public static boolean isAbsent(String value) {
        return value == null;
    }

    public static String buildSetCookieString(String cookieKey,
            String cookieValue, String cookieDomain, long cookieExpire) {
        StringBuilder builder = new StringBuilder();
        builder.append(cookieKey);
        builder.append("=");
        builder.append(cookieValue);
        builder.append("; Domain=");
        builder.append(cookieDomain);
        builder.append("; Expires=");
        Date dateExpires = new Date();
        long expires = cookieExpire;
        dateExpires.setTime(dateExpires.getTime() + expires);
        DateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy kk:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        builder.append(dateFormat.format(dateExpires));
        return builder.toString();
    }

    public static String buildResetCookieString(String cookieKey,
            String cookieDomain) {
        return buildSetCookieString(cookieKey, "", cookieDomain, -1000);
    }
}
