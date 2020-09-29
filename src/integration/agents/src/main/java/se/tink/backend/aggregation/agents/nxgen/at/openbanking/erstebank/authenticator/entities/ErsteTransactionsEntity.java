package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionLinksWithHrefEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionsGenericEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErsteTransactionsEntity extends TransactionsGenericEntity {

    @JsonProperty("_links")
    private TransactionLinksWithHrefEntity links;

    public boolean hasMore() {
        return Optional.ofNullable(links)
                .map(TransactionLinksWithHrefEntity::hasNextLink)
                .orElse(false);
    }

    public String getNextLink() {
        return links.getNextLink();
    }
}
