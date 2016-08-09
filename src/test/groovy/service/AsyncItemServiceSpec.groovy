package service

import app.Application
import dto.Item
import groovy.util.logging.Slf4j
import rx.Observable
import rx.observers.TestSubscriber
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.Assert.assertEquals

@Slf4j
class AsyncItemServiceSpec extends Specification {

    AsyncItemService asyncItemService

    def setupSpec() {
        println "@BeforeClass"
    }

    def cleanupSpec() {
        println "@AfterClass"
    }

    def setup() {
        asyncItemService = new AsyncItemService()
        Application.INSTANCE.cache.cleanUp()
    }

    def cleanup() {
        println "@After"
    }

    def 'Single thread concurrency -> THIS IS WRONG!'() {

        setup:
        TestSubscriber<String> testSubscriber = new TestSubscriber<>()

        and: 'An existing item id'
        String anItemId = 'MLA623187668'

        when: "service is invoked to get and item from it's id"
        Observable<Item> observable = asyncItemService.getById(anItemId)
                .doOnNext({ Item anItem ->
            // Debug bussines
            log.debug "Test Running on ${Thread.currentThread().name}"
        })

        observable.subscribe(testSubscriber);

        then:

        testSubscriber.awaitTerminalEvent()
        log.info 'awaiting Terminal Event'

        testSubscriber.assertNoErrors()

        assertThat(observable).isNotNull()

        Item cachedValue = Application.INSTANCE.cache.getIfPresent('/items/' + anItemId)
        assertThat(cachedValue).isNotNull()

        testSubscriber.assertValue(cachedValue)

        String observingThread = testSubscriber.getLastSeenThread().getName();
        log.info "Last seen thread $observingThread"

    }

    def 'Single thread concurrency on multiples threads'() {

        setup:
        TestSubscriber<String> testSubscriber = new TestSubscriber<>()

        and: 'An existing item id'
        Collection<String> severalItemsIds = Arrays.asList('MLA623187668', 'MLA622553436')

        when: "service is invoked to get and item from it's id"
        Observable<Item> observable = asyncItemService.getByIds(severalItemsIds)
                .doOnNext({ Item anItem ->
            // Debug bussines
            log.debug "Test Running on ${Thread.currentThread().name}"
        })

        observable.subscribe(testSubscriber);

        then:

        testSubscriber.awaitTerminalEvent()
        log.info 'awaiting Terminal Event'

        testSubscriber.assertNoErrors()

        assertThat(observable).isNotNull()

        assertEquals 'Cached items are not 2', 2, Application.INSTANCE.cache.size()

        Collection<Item> cachedValues = severalItemsIds.collect {
            Application.INSTANCE.cache.getIfPresent('/items/' + it) as Item
        }


        assertThat(cachedValues)
                .isNotEmpty()
                .hasSize(2)

        Item[] values = cachedValues.toArray(new Item[cachedValues.size()]);

        testSubscriber.assertValues(values)

        String observingThread = testSubscriber.getLastSeenThread().getName();
        log.info "Last seen thread $observingThread"

    }

}
