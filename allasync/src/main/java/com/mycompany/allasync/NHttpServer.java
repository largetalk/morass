package com.mycompany.allasync;

  
import java.io.File;  
import java.io.IOException;  
import java.io.InterruptedIOException;  
import java.net.InetSocketAddress;  
import java.net.URL;  
import java.net.URLDecoder;  
import java.security.KeyStore;  
import java.util.Locale;  
  
import javax.net.ssl.KeyManager;  
import javax.net.ssl.KeyManagerFactory;  
import javax.net.ssl.SSLContext;  
  
import org.apache.http.HttpException;  
import org.apache.http.HttpRequest;  
import org.apache.http.HttpResponse;  
import org.apache.http.HttpResponseInterceptor;  
import org.apache.http.HttpStatus;  
import org.apache.http.MethodNotSupportedException;  
import org.apache.http.entity.ContentType;  
import org.apache.http.impl.DefaultConnectionReuseStrategy;  
import org.apache.http.impl.nio.DefaultNHttpServerConnection;  
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;  
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;  
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;  
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;  
import org.apache.http.nio.NHttpConnection;  
import org.apache.http.nio.NHttpConnectionFactory;  
import org.apache.http.nio.NHttpServerConnection;  
import org.apache.http.nio.entity.NFileEntity;  
import org.apache.http.nio.entity.NStringEntity;  
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;  
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;  
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;  
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;  
import org.apache.http.nio.protocol.HttpAsyncRequestHandlerRegistry;  
import org.apache.http.nio.protocol.HttpAsyncExchange;  
import org.apache.http.nio.protocol.HttpAsyncService;  
import org.apache.http.nio.reactor.IOEventDispatch;  
import org.apache.http.nio.reactor.ListeningIOReactor;  
import org.apache.http.params.CoreConnectionPNames;  
import org.apache.http.params.CoreProtocolPNames;  
import org.apache.http.params.HttpParams;  
import org.apache.http.params.SyncBasicHttpParams;  
import org.apache.http.protocol.ExecutionContext;  
import org.apache.http.protocol.HttpContext;  
import org.apache.http.protocol.HttpProcessor;  
import org.apache.http.protocol.ImmutableHttpProcessor;  
import org.apache.http.protocol.ResponseConnControl;  
import org.apache.http.protocol.ResponseContent;  
import org.apache.http.protocol.ResponseDate;  
import org.apache.http.protocol.ResponseServer;  
  
/** 
 * HTTP/1.1 file server based on the non-blocking I/O model and capable of direct channel 
 * (zero copy) data transfer. 
 */  
public class NHttpServer {  
  
    public static void main(String[] args) throws Exception {  
        if (args.length < 1) {  
            System.err.println("Please specify document root directory");  
            System.exit(1);  
        }  
        // Document root directory  
        File docRoot = new File(args[0]);  
        int port = 8080;  
        if (args.length >= 2) {  
            port = Integer.parseInt(args[1]);  
        }  
        // HTTP parameters for the server  
        HttpParams params = new SyncBasicHttpParams();  
        params  
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)  
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)  
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)  
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpTest/1.1");  
        // Create HTTP protocol processing chain  
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {  
                // Use standard server-side protocol interceptors  
                new ResponseDate(),  
                new ResponseServer(),  
                new ResponseContent(),  
                new ResponseConnControl()  
        });  
        // Create request handler registry  
        HttpAsyncRequestHandlerRegistry reqistry = new HttpAsyncRequestHandlerRegistry();  
        // Register the default handler for all URIs  
        reqistry.register("*", new NHttpRequestHandler(docRoot));  
        // Create server-side HTTP protocol handler  
        HttpAsyncService protocolHandler = new HttpAsyncService(  
                httpproc, new DefaultConnectionReuseStrategy(), reqistry, params) {  
  
            @Override  
            public void connected(final NHttpServerConnection conn) {  
                System.out.println(conn + ": connection open");  
                super.connected(conn);  
            }  
  
            @Override  
            public void closed(final NHttpServerConnection conn) {  
                System.out.println(conn + ": connection closed");  
                super.closed(conn);  
            }  
  
        };  
        // Create HTTP connection factory  
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;  
        if (port == 8443) {  
            // Initialize SSL context  
            ClassLoader cl = NHttpServer.class.getClassLoader();  
            URL url = cl.getResource("my.keystore");  
            if (url == null) {  
                System.out.println("Keystore not found");  
                System.exit(1);  
            }  
            KeyStore keystore  = KeyStore.getInstance("jks");  
            keystore.load(url.openStream(), "secret".toCharArray());  
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(  
                    KeyManagerFactory.getDefaultAlgorithm());  
            kmfactory.init(keystore, "secret".toCharArray());  
            KeyManager[] keymanagers = kmfactory.getKeyManagers();  
            SSLContext sslcontext = SSLContext.getInstance("TLS");  
            sslcontext.init(keymanagers, null, null);  
            connFactory = new SSLNHttpServerConnectionFactory(sslcontext, null, params);  
        } else {  
            connFactory = new DefaultNHttpServerConnectionFactory(params);  
        }  
        // Create server-side I/O event dispatch  
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);  
        // Create server-side I/O reactor  
        ListeningIOReactor ioReactor = new DefaultListeningIOReactor();  
        try {  
            // Listen of the given port  
            ioReactor.listen(new InetSocketAddress(port));  
            // Ready to go!  
            ioReactor.execute(ioEventDispatch);  
        } catch (InterruptedIOException ex) {  
            System.err.println("Interrupted");  
        } catch (IOException e) {  
            System.err.println("I/O error: " + e.getMessage());  
        }  
        System.out.println("Shutdown");  
    }  
 
  
}  