package se.tink.backend.common.concurrency;

import com.google.common.collect.ComparisonChain;
import java.util.Comparator;

public class PrioritizedRunnableComparator implements Comparator<PrioritizedRunnable> {

    @Override
    public int compare(PrioritizedRunnable o1, PrioritizedRunnable o2) {
        return ComparisonChain.start()
                .compare(o1.priority, o2.priority)
                .result();
    }

}
