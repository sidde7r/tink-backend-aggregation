package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransaction {

    private AmountEntity amount;
    private String details;

//    Other possible fields:
//    private String merchant;
//    private String info;
//    private String location;

    @JsonFormat(pattern = "y-M-d")
    private Date date;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount account) {
        return CreditCardTransaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDescription(details)
                .setDate(date)
                .setCreditAccount(account)
                .build()
                ;
    }
}
