package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.BookedEntity;

@Getter
@Setter
public class TransactionsEntity {

    private List<BookedEntity> booked;

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    public TransactionLinksEntity getLinks() {
        return Optional.ofNullable(links)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.MISSING_LINKS_OBJECT));
    }
}
