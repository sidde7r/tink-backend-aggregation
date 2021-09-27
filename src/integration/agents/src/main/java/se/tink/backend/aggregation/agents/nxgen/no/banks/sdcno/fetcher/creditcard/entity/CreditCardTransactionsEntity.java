package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardTransactionsEntity {

    @JsonProperty("transaksjoner")
    private List<CreditCardBookedTransactionEntity> bookedTransactions;

    @JsonProperty("reservasjoner")
    private List<CreditCardPendingTransactionEntity> pendingTransactions;
}
