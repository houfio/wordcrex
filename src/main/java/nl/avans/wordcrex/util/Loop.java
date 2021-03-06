package nl.avans.wordcrex.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Loop {
    private final List<ScheduledFuture<?>> futures;

    private Loop(List<ScheduledFuture<?>> futures) {
        this.futures = futures;
    }

    public static Loop start(Map<Integer, Runnable> loops, Consumer<Exception> consumer) {
        var pool = Executors.newScheduledThreadPool(loops.size());
        var executors = new ArrayList<ScheduledFuture<?>>();

        for (var loop : loops.entrySet()) {
            executors.add(pool.scheduleAtFixedRate(() -> {
                try {
                    loop.getValue().run();
                } catch (Exception e) {
                    consumer.accept(e);
                }
            }, 0, 1000 / loop.getKey(), TimeUnit.MILLISECONDS));
        }

        return new Loop(executors);
    }

    public void stop() {
        this.futures.forEach((future) -> future.cancel(true));
    }
}
