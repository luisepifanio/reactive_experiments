package service

import backtolife.AsyncUtils
import dto.Item
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import rx.Observable

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT

// import static groovyx.net.http.ContentType.XML
class ItemService {
    Item getById(String id) {
        long start = System.currentTimeMillis()

        RESTClient client = new RESTClient("https://api.mercadolibre.com/items/$id")
        HttpResponseDecorator response = client.get(
                contentType: JSON, //TEXT
                requestContentType: JSON,
                headers: [
                        Accept: JSON.getAcceptHeader()
                ]
        )

        def data = response.data

        Item item = Item.builder()
                .id(data.id)
                .title(data.title)
                .currencyId(data.currency_id)
                .thumbnail(data.thumbnail)
                .sellerId(data.seller_id)
                .price(data.price as BigDecimal)
            .build()

        long end = System.currentTimeMillis()
        TimeDuration duration = TimeCategory.minus(new Date(end), new Date(start))
        println duration

        return item
    }


    Observable<Item> getByIdAsync(final String id) {
        CompletableFuture<Item> future = CompletableFuture.supplyAsync(
                new Supplier<Item>() {
                    @Override
                    Item get() {
                        ItemService.this.getById(id)
                    }
                }
        );
        return AsyncUtils.toObservable(future);
    }
}
