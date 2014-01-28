/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.allasync;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Locale;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;


public class NHttpRequestHandler implements HttpAsyncRequestHandler<HttpRequest> {  
  
        private final File docRoot;  
  
        public NHttpRequestHandler(final File docRoot) {  
            super();  
            this.docRoot = docRoot;  
        }  
  
        public HttpAsyncRequestConsumer<HttpRequest> processRequest(  
                final HttpRequest request,  
                final HttpContext context) {  
            // Buffer request content in memory for simplicity  
            return new BasicAsyncRequestConsumer();  
        }  
  
        public void handle(  
                final HttpRequest request,  
                final HttpAsyncExchange httpexchange,  
                final HttpContext context) throws HttpException, IOException {  
            HttpResponse response = httpexchange.getResponse();  
            handleInternal(request, response, context);  
            httpexchange.submitResponse(new BasicAsyncResponseProducer(response));  
        }  
  
        private void handleInternal(  
                final HttpRequest request,  
                final HttpResponse response,  
                final HttpContext context) throws HttpException, IOException {  
  
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);  
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {  
                throw new MethodNotSupportedException(method + " method not supported");  
            }  
  
            String target = request.getRequestLine().getUri();  
            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));  
            if (!file.exists()) {  
  
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);  
                NStringEntity entity = new NStringEntity(  
                        "<html><body><h1>File" + file.getPath() +  
                        " not found</h1></body></html>",  
                        ContentType.create("text/html", "UTF-8"));  
                response.setEntity(entity);  
                System.out.println("File " + file.getPath() + " not found");  
  
            } else if (!file.canRead() || file.isDirectory()) {  
  
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);  
                NStringEntity entity = new NStringEntity(  
                        "<html><body><h1>Access denied</h1></body></html>",  
                        ContentType.create("text/html", "UTF-8"));  
                response.setEntity(entity);  
                System.out.println("Cannot read file " + file.getPath());  
  
            } else {  
                NHttpConnection conn = (NHttpConnection) context.getAttribute(  
                        ExecutionContext.HTTP_CONNECTION);  
                response.setStatusCode(HttpStatus.SC_OK);  
                NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));  
                response.setEntity(body);  
                System.out.println(conn + ": serving file " + file.getPath());  
            }  
        }  
  
    }