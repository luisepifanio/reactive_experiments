package backtolife;

import rx.Observable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncUtils {

    // http://www.nurkiewicz.com/2014/11/converting-between-completablefuture.html

    public static <T> Observable<T> toObservable(CompletableFuture<T> future) {
        return Observable.create(subscriber ->
                future.whenComplete((result, error) -> {
                    if (error != null) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(error);
                        }

                    } else {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(result);
                            subscriber.onCompleted();
                        }
                    }
                }));
    }

    public static <T> CompletableFuture<List<T>> fromObservable(Observable<T> observable) {
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        observable
                .doOnError(future::completeExceptionally)
                .toList()
                .forEach(future::complete);
        return future;
    }

    public static <T> CompletableFuture<T> fromSingleObservable(Observable<T> observable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        observable
                .doOnError(future::completeExceptionally)
                .single()
                .forEach(future::complete);
        return future;
    }
}
