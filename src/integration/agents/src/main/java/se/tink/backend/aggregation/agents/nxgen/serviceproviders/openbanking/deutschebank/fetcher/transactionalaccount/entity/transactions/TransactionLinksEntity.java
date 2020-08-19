package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionLinksEntity {

    @JsonProperty("next")
    private Href nextLink;

    public boolean hasNextLink() {
        return Optional.ofNullable(nextLink).map(Href::getHref).isPresent();
    }
}
