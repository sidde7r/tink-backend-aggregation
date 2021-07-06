package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.Href;

public class TransactionPaginationLinksEntity {

    private Href self;
    private Href account;
    private Href previous;
    private Href next;

    public boolean hasMore() {
        return next != null && !Strings.isNullOrEmpty(next.getHref());
    }

    public String getNext() {
        return hasMore() ? next.getHref() : null;
    }
}
