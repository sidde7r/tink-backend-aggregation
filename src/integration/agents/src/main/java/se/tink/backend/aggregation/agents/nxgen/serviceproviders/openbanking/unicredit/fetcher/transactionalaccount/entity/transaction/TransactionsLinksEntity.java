package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {
    private LinkEntity next;

    public String getNext() {
        return hasNext() ? next.getHref() : null;
    }

    public boolean hasNext() {
        return next != null && !Strings.isNullOrEmpty(next.getHref());
    }
}
