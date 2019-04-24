package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionPaginationLinksEntity {

    /*
     The documentation in SEB website might be not accurate:
     Description of _links in their document is not matching with what they
     actually send.
    */

    public LinksDetailsEntity account;
    public LinksDetailsEntity next;

    public boolean hasMore() {
        return next != null && next.href != null && !Strings.isNullOrEmpty(next.href);
    }

    public String getNext() {
        return (hasMore()) ? next.href : null;
    }
}
