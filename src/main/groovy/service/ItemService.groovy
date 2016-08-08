package service

import app.Application
import backtolife.AsyncUtils
import com.google.common.base.Preconditions
import dto.Item
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import rx.Observable

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

import static groovyx.net.http.ContentType.JSON

class ItemService {

    Item getById(String id) {

        Application app = Application.INSTANCE

        app.cache.get("/items/$id".toString(), {
            long start = System.currentTimeMillis()
            println "Started $id"

            Future futureResponse = getFutureItemById()

            // This is a BLOCKING approach ok?
            // You should avoid this as a plague
            def data = futureResponse.get(id)

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

            println "getById $id took $duration"

            return item
        } as Callable<Item>) as Item
    }

    Collection<Item> getItemsByIdsBlocking(Collection<String> itemIds) {
        Preconditions.checkNotNull(itemIds, 'Items');

        itemIds.parallelStream()
                .distinct()
                .map(new Function<String, Item>() {
                            @Override
                            Item apply(String it) {
                                getById(it)
                            }
                }).collect(Collectors.toList())
    }

    public static Future getFutureItemById(String id) {

        Application app = Application.INSTANCE

        Future futureResponse = app.client.get(
                path: "/items/$id",
                //body: [ status: msg, source: 'httpbuilder' ],
                contentType: JSON,
                //requestContentType: JSON,
                //query :[:]
                headers: [
                        Accept: JSON.getAcceptHeader()
                ]
        )
        futureResponse
    }

    Observable<List<Item>> getItemsByIds(Collection<String> itemIds) {

        Collection<CompletableFuture<Item>> futures = new HashSet<String>(itemIds).collect {
            CompletableFuture.supplyAsync(
                    new Supplier<Item>() {
                        @Override
                        Item get() {
                            ItemService.this.getById(it)
                        }
                    }
            )
        }

        return AsyncUtils.toObservable(futures)
                .toList()
    }


    Observable<Item> getByIdAsync(final String id) {
        CompletableFuture<Item> future = CompletableFuture.supplyAsync(
                new Supplier<Item>() {
                    @Override
                    Item get() {
                        ItemService.this.getById(id)
                    }
                }
        )
        return AsyncUtils.toObservable(future);
    }
}
