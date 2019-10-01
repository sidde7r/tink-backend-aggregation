package se.tink.backend.aggregation.nxgen.http.filter.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrderedFiltersSorter extends AbstractOrderedFiltersSorter
        implements OrderedFiltersSorter {

    private final Logger log = LoggerFactory.getLogger(DefaultOrderedFiltersSorter.class);
    private static DefaultOrderedFiltersSorter INSTANCE;

    private DefaultOrderedFiltersSorter() {
        super(FilterPhases.asDefaultOrderedList());
    }

    public static synchronized OrderedFiltersSorter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultOrderedFiltersSorter();
        }

        return INSTANCE;
    }
}
