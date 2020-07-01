package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.BookedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<BookedEntity> booked;

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    public List<BookedEntity> getBooked() {
        return Optional.ofNullable(booked).orElseGet(Collections::emptyList);
    }

    public TransactionLinksEntity getLinks() {
        return Optional.ofNullable(links)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.MISSING_LINKS_OBJECT));
    }
}
