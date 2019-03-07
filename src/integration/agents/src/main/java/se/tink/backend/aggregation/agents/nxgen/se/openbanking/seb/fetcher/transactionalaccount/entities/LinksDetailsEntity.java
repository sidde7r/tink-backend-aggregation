package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksDetailsEntity {
    @JsonProperty
    private String href;

    public boolean hasMore() {
        return !Strings.isNullOrEmpty(href) && href.contains(SEBConstants.QueryKeys.TRANSACTION_SEQUENCE_NUMBER);
    }
}
