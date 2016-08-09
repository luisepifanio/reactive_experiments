package app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import groovyx.net.http.AsyncHTTPBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static groovyx.net.http.ContentType.JSON;

/**
 * Created by lepifanio on 4/8/16.
 */
@Slf4j
public enum Application {
    INSTANCE;

    public AsyncHTTPBuilder client;
    public Cache<String,Object> cache;


    Application() {

        Map<String, Object> args = new LinkedHashMap<>();

        args.put("poolSize", 20);
        args.put("uri", "https://api.mercadolibre.com");
        args.put("contentType", JSON);
        args.put("timeout", 12_000);

        try {
            client = new AsyncHTTPBuilder(args);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // Maximun desirable test run
                .expireAfterAccess(5, TimeUnit.MINUTES) // Maximun desirable test run
                .maximumSize(1000)
                .build();


    }
}
