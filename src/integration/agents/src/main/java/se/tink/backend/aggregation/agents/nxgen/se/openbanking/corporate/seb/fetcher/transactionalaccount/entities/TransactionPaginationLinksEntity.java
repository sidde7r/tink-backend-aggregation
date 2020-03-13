package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.fetcher.transactionalaccount.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionPaginationLinksEntity {

    public LinksDetailsEntity account;
    public LinksDetailsEntity next;

    public boolean hasMore() {
        return next != null && !Strings.isNullOrEmpty(next.href);
    }

    public String getNext() {
        return hasMore() ? next.href : null;
    }
}
