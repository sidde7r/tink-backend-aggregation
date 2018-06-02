package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValueEncodedTriplet;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.core.Amount;

@JsonObject
public class FutureTransactionDto {
    private TypeValuePair currencyCode;
    private TypeValuePair transactionTypeCode;
    private TypeValuePair productTypeCode;
    private TypeValueEncodedTriplet referenceNo;
    private TypeValuePair executionDate;
    private TypeValuePair amount;
    private TypeValuePair amountIndicator;
    private TypeValuePair descriptionLine01;
    private TypeValuePair descriptionLine02;
    private TypeValuePair detailIndicator;

    public UpcomingTransaction toUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(Amount.inEUR(Double.valueOf(amount.getValue())))
                .setDescription(getDescription())
                .setDate(parseExecutionDate())
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        if (descriptionLine02 != null && !Strings.isNullOrEmpty(descriptionLine02.getValue())) {
            return descriptionLine02.getValue();
        } else {
            return descriptionLine01.getValue();
        }
    }

    private Date parseExecutionDate() {
        try {
            return DateUtils.parseDate(
                    executionDate.getValue(), "ddMMyyyy");
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
