package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account.BookedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<BookedEntity> booked;

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    public List<BookedEntity> getBooked() {
        return Optional.ofNullable(booked).orElseGet(Lists::emptyList);
    }

    public TransactionLinksEntity getLinks() {
        return Optional.ofNullable(links)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.MISSING_LINKS_OBJECT));
    }
}
