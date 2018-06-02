package se.tink.backend.common.concurrency;

import com.google.common.collect.ComparisonChain;
import java.util.Comparator;

public class PrioritizedCallableComparator implements Comparator<PrioritizedCallable<?>> {

    @Override
    public int compare(PrioritizedCallable<?> o1, PrioritizedCallable<?> o2) {
        return ComparisonChain.start()
                .compare(o1.priority, o2.priority)
                .result();
    }

}
