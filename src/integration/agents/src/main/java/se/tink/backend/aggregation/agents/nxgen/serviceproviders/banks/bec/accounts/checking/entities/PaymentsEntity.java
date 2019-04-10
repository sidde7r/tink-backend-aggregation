package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class PaymentsEntity {
    private String paymentType;
    private String urlDetail;
    private String accountFrom;
    private String accountTo;

    @JsonFormat(pattern = "y-M-d")
    private Date date;

    private String dateTxt;
    private double amount;
    private String amountTxt;
    private String currency;
    private String statusText;
    private String textReceiver;
    private String iconId;

    @JsonIgnore
    public UpcomingTransaction toTinkUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setDate(this.date)
                .setAmount(new Amount(this.currency, this.amount))
                .setDescription(getDescription())
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        return Strings.isNullOrEmpty(this.textReceiver) ? this.accountTo : this.textReceiver;
    }
}
