package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionPaginationLinksEntity {

    private LinksDetailsEntity account;
    private LinksDetailsEntity next;

    public boolean hasMore() {
        return next != null && !Strings.isNullOrEmpty(next.getHref());
    }

    public String getNext() {
        return hasMore() ? next.getHref() : null;
    }
}
