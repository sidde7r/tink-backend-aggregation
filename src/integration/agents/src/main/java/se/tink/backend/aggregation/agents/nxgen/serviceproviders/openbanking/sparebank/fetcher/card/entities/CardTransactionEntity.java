package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.BalanceAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardTransactionEntity {

    private String cardTransactionId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private BalanceAmountEntity transactionAmount;
    private BalanceAmountEntity originalAmount;

    private Boolean invoiced;
    private String transactionDetails;
}
