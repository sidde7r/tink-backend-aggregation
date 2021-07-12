package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.DateFieldMigration;
import se.tink.backend.aggregation.nxgen.core.transaction.FieldsMigrations;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class BookedEntity {

    @JsonFormat(pattern = Formats.TRANSACTION_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = Formats.TRANSACTION_DATE_FORMAT)
    private Date valueDate;

    private AmountEntity amount;
    private String remittanceInformationUnstructured;

    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setAmount(amount.toTinkAmount())
                        .setDate(bookingDate)
                        .setDescription(remittanceInformationUnstructured);
        if (null != valueDate) {
            builder.setTransactionDates(createTransactionDates());
            builder.setFieldsMigrations(
                    FieldsMigrations.builder()
                            .migration(DateFieldMigration.version1(valueDate))
                            .build());
        }
        return builder.build();
    }

    private TransactionDates createTransactionDates() {
        LocalDate localValueDate =
                LocalDate.from(valueDate.toInstant().atZone(ZoneId.systemDefault()));
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation(localValueDate))
                .build();
    }
}
