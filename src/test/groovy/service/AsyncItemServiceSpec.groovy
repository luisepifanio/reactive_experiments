package service

import app.Application
import dto.Item
import groovy.util.logging.Slf4j
import rx.Observable
import rx.observers.TestSubscriber
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

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

}
