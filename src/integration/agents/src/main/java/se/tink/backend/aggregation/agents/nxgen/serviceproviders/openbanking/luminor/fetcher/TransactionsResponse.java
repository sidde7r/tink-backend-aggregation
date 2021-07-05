package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.BookedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class TransactionsResponse {
    @Getter private AccountEntity account;

    @Getter
    @JsonProperty("booked")
    private List<BookedEntity> booked;

    @JsonProperty("_links")
    private LinksEntity links;

    public List<AggregationTransaction> getTinkTransactions() {
        List<AggregationTransaction> transactions = new ArrayList<>();
        if (booked != null) {
            transactions.addAll(
                    booked.stream()
                            .map(BookedEntity::toTinkTransaction)
                            .collect(Collectors.toList()));
        }
        return transactions;
    }
}
