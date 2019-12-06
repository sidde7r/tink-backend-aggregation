package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationEntity {

    private LinkEntity first;
    private LinkEntity last;
    private LinkEntity next;
    private LinkEntity prev;
    private LinkEntity self;
    private LinkEntity parentList;

    public String getNext() {
        return hasNext() ? next.getHref() : null;
    }

    public boolean hasNext() {
        return next != null && !Strings.isNullOrEmpty(next.getHref());
    }
}
