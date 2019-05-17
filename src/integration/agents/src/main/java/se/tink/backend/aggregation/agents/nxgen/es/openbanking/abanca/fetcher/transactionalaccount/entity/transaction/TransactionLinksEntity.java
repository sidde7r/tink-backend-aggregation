package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.transaction;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksEntity {

    private String self;
    private String first;
    private String next;
    private String prev;
    private String last;

    public String getNext() {
        return next;
    }

    public boolean hasNext() {
        return !Strings.isNullOrEmpty(next);
    }
}
