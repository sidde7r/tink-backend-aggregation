package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common.TransactionAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {
    protected TransactionAmountEntity transactionAmount;
    protected String remittanceInformationUnstructured;
    protected String remittanceInformationStructured;

    public TransactionDates getTinkTransactionDates(LocalDate valueDate, LocalDate bookingDate) {
        TransactionDates.Builder builder = TransactionDates.builder();

        if (valueDate != null) {
            builder.setValueDate(new AvailableDateInformation().setDate(valueDate));
        }

        if (bookingDate != null) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }

    protected String getDescription() {
        if (StringUtils.isNumeric(remittanceInformationUnstructured)
                && Transactions.SALARY_PATTERN
                        .matcher(Strings.nullToEmpty(remittanceInformationStructured).toLowerCase())
                        .matches()) {
            return remittanceInformationStructured;
        }

        return Optional.ofNullable(remittanceInformationUnstructured)
                .orElse(remittanceInformationStructured);
    }
}
