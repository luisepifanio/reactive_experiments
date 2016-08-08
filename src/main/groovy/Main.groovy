import app.Application
import dto.Item
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovyx.net.http.HTTPBuilder
import rx.Observable
import rx.schedulers.Schedulers
import service.ItemService

import java.util.concurrent.Future

import static groovyx.net.http.ContentType.JSON

println 'Running Reactive Experiments'


HTTPBuilder client = Application.INSTANCE.client


List<String> items = [
        'MLA623187668','MLA622553436','MLC435153885','MLA604489602' //,
        //'MLA623187668','MLA622553436','MLA604489602',
        //'MLC435153885',
        //'MLX987654321'
]

String uniqueItemId = 'MLA623187668'

println new ItemService().getById(uniqueItemId).toString()

Future futureResponse = client.get(
        path: "/items/$uniqueItemId",
        //body: [ status: msg, source: 'httpbuilder' ],
        contentType: JSON,
        //requestContentType: JSON,
        //query :[:]
        headers: [
                Accept: JSON.getAcceptHeader()
        ]
)

assert futureResponse instanceof java.util.concurrent.Future

println ':::->' + futureResponse.get()

// same thread result
Observable.from(futureResponse)
        .subscribeOn(Schedulers.computation())
        .map({
    Item.builder()
            .id(it.id)
            .title(it.title)
            .currencyId(it.currency_id)
            .thumbnail(it.thumbnail)
            .sellerId(it.seller_id)
            .price(it.price as BigDecimal)
            .build()
})
        .subscribe({ Item item ->
    println "Subscriber received $item.id on ${Thread.currentThread().getName()}"
});

long start = System.currentTimeMillis()

Observable.from(new ArrayList<>(items))
        .subscribeOn(Schedulers.computation())
        .flatMap({ String id ->
            // Convert to future
            println "map $id to Future on ${Thread.currentThread().getName()} and took"
            Future aFuture = client.get(
                    path: "/items/$id",
                    //body: [ status: msg, source: 'httpbuilder' ],
                    contentType: JSON,
                    //requestContentType: JSON,
                    //query :[:]
                    headers: [
                            Accept: JSON.getAcceptHeader()
                    ]
            )

            Observable<Item> item = Observable.from(futureResponse)
                    .subscribeOn(Schedulers.computation())
                    .map({
                        Item.builder()
                                .id(it.id)
                                .title(it.title)
                                .currencyId(it.currency_id)
                                .thumbnail(it.thumbnail)
                                .sellerId(it.seller_id)
                                .price(it.price as BigDecimal)
                                .build()
                    })
            return item

        })
        .toList()
        .subscribe({ List<Item> itemList ->
    long end = System.currentTimeMillis()
    TimeDuration duration = TimeCategory.minus(new Date(end), new Date(start))
    println "Subscriber received ${itemList.size()} items on ${Thread.currentThread().getName()} and took $duration"
});




/*
ItemService itemService = new ItemService();


itemService.getByIdAsync("MLC435153885")
        .timeout(700, TimeUnit.MILLISECONDS,Observable.just(null))
        .subscribe(System.out.&println);


long start = System.currentTimeMillis()

List<String> items = [
         'MLA623187668','MLA622553436','MLC435153885','MLA604489602',
         'MLA623187668','MLA622553436','MLA604489602',
        'MLC435153885',
        //'MLX987654321'
]
Collections.shuffle(items, new Random(start));


itemService.getItemsByIds(items)
        .timeout(7000, TimeUnit.MILLISECONDS, Observable.just(null))
        .subscribe(
        { Collection<Item> it ->
            println "onNext! $it"
        },
        { Throwable t ->
            println "onError! $t"
        },
        {
            println "onComplete! $it"

            long end = System.currentTimeMillis()
            TimeDuration duration = TimeCategory.minus(new Date(end), new Date(start))
            println "getItemsByIds took $duration"
        });
*/



def readln = javax.swing.JOptionPane.&showInputDialog
def username = readln 'Enter to fisnish'
