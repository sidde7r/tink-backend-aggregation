package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceAmountBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CardTransactionEntity {

    private String cardTransactionId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private BalanceAmountBaseEntity transactionAmount;
    private String transactionDetails;
    private boolean invoiced;
}
