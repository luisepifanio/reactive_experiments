package service

import app.Application
import dto.Item
import functions.MapperFunctions
import groovy.util.logging.Slf4j
import rx.Observable
import rx.schedulers.Schedulers

@Slf4j
class AsyncItemService {

    /**
     * Non-blocking method that immediately returns the value
     * if available or  uses a thread to fetch the value and
     * caches value onNext to
     */
    Observable<Item> getById(String id) {
        String key = '/items/' + id
        Item cachedItem = Application.INSTANCE.cache.getIfPresent(key) as Item

        if (cachedItem) {
            // if data available return immediately with data
            return Observable.just(cachedItem)
        } else {
            // else spawn thread or async IO to fetch data
            return Observable.from(ItemService.getFutureItemById(id))
                    .subscribeOn(Schedulers.io()) // It is preferable to run in IO Thread
                    .flatMap({ val -> Observable.just(val) })
                    .map({ Map it -> MapperFunctions.mapToItemFunction(it) })
                    .doOnNext({ Item anItem ->
                        // It is better to avoid previous result on its own
                        // on next to avoid side effects
                        Application.INSTANCE.cache.put(key,anItem)
                    }).doOnNext({ Item anItem ->
                        // Debug bussines
                        log.debug "Running on ${Thread.currentThread().name}"
                    })
        }
    }

    Observable<Item> getByIds(final Collection<String> multipleIds) {
        Collection<Item> source = multipleIds ?: []
        if( source.isEmpty() ) return Observable.empty()

        return Observable.from(multipleIds)
                // It is preferable to run in IO Thread
                .subscribeOn(Schedulers.io())
                // flatMap is a nice way to
                .flatMap({ val -> Observable.just(val) })
                .map({ Map it -> MapperFunctions.mapToItemFunction(it) })
                .doOnNext({ Item anItem ->
                    // It is better to avoid previous result on its own
                    // on next to avoid side effects
                    Application.INSTANCE.cache.put(key,anItem)
                }).doOnNext({ Item anItem ->
                    // Debug bussines
                    log.debug "Running on ${Thread.currentThread().name} and returned $anItem"
                })



    }
}
