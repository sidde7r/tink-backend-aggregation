package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValueEncodedTriplet;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;

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

    public TypeValuePair getExecutionDate() {
        return executionDate;
    }

    public UpcomingTransaction toUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(Amount.inEUR(Double.valueOf(amount.getValue())))
                .setDescription(descriptionLine01.getValue())
                .setRawDetails(getRawDetails())
                .setDate(parseExecutionDate())
                .build();
    }

    private Date parseExecutionDate() {
        try {
            return DateUtils.parseDate(executionDate.getValue(), "ddMMyyyy");
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @JsonIgnore
    private RawDetails getRawDetails() {
        if (descriptionLine02 == null
                || com.google.common.base.Strings.isNullOrEmpty(descriptionLine02.getValue())) {
            return null;
        }

        return new RawDetails(descriptionLine02.getValue());
    }

    @JsonObject
    public class RawDetails {
        private List<String> details;

        public RawDetails(String details) {
            this.details = Collections.singletonList(details);
        }

        public List<String> getDetails() {
            return details;
        }
    }
}
