package backtolife;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncUtils {

    // http://www.nurkiewicz.com/2014/11/converting-between-completablefuture.html

    public static <T> Observable<T> toObservable(CompletableFuture<? extends T> future) {
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

    public static <T> Observable<T> toObservable(Collection<CompletableFuture<? extends T>> futures) {
        if(futures == null || futures.size() < 1 ){
            throw  new IllegalArgumentException("Please just transform a non empty Collection of futures");
        }

        List<Observable<T>>  observables = new ArrayList<>();
        for (CompletableFuture<? extends T> aFuture : futures) {
            observables.add(toObservable(aFuture));
        }

        return Observable.from(observables)
                //execute in parallel
                .flatMap(task -> task.observeOn(Schedulers.computation()));
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
