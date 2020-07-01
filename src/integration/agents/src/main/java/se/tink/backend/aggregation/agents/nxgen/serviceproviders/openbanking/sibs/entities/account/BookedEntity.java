package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.transaction.CreditorAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class BookedEntity {

    @JsonFormat(pattern = Formats.TRANSACTION_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = Formats.TRANSACTION_DATE_FORMAT)
    private Date valueDate;

    private AmountEntity amount;
    private CreditorAccount creditorAccount;
    private String remittanceInformationUnstructured;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDate(valueDate)
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
