package src.libraries.interaction_counter.local;

import java.util.concurrent.atomic.AtomicInteger;
import src.libraries.interaction_counter.InteractionCounter;

public class LocalInteractionCounter implements InteractionCounter {

    private final AtomicInteger counter = new AtomicInteger(0);

    public void inc() {
        counter.incrementAndGet();
    }

    public int getNumberInteractions() {
        return counter.get();
    }
}
