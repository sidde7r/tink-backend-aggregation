package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.transaction.CreditorAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.DateFieldMigration;
import se.tink.backend.aggregation.nxgen.core.transaction.FieldsMigrations;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;

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
        Builder builder =
                Transaction.builder()
                        .setAmount(amount.toTinkAmount())
                        .setDate(bookingDate)
                        .setDescription(remittanceInformationUnstructured);
        if (null != valueDate) {
            builder.setFieldsMigrations(
                    FieldsMigrations.builder()
                            .migration(DateFieldMigration.version1(valueDate))
                            .build());
        }
        return builder.build();
    }
}
