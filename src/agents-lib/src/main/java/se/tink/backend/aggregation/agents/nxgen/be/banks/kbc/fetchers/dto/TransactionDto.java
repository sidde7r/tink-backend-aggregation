package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonObject
public class TransactionDto {
    private TypeValuePair transactionDate;
    private TypeValuePair amount;
    private TypeValuePair appendixType;
    private TypeValuePair descriptionLine01;
    private TypeValuePair descriptionLine02;
    private TypeValuePair subAccountNo;
    private TypeValuePair registrationTs;
    private TypeValuePair bookingDate;

    public TypeValuePair getTransactionDate() {
        return transactionDate;
    }

    public TypeValuePair getAmount() {
        return amount;
    }

    public TypeValuePair getAppendixType() {
        return appendixType;
    }

    public TypeValuePair getDescriptionLine01() {
        return descriptionLine01;
    }

    public TypeValuePair getDescriptionLine02() {
        return descriptionLine02;
    }

    public TypeValuePair getSubAccountNo() {
        return subAccountNo;
    }

    public TypeValuePair getRegistrationTs() {
        return registrationTs;
    }

    public TypeValuePair getBookingDate() {
        return bookingDate;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(Double.valueOf(amount.getValue())))
                .setDescription(descriptionLine01.getValue())
                .setRawDetails(getRawDetails())
                .setDate(parseTransactionDate())
                .build();
    }

    private Date parseTransactionDate() {
        try {
            return DateUtils.parseDate(
                    registrationTs.getValue().substring(0, 23), "yyyy-MM-dd-HH.mm.ss.SSS");
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @JsonIgnore
    private RawDetails getRawDetails() {
        if (descriptionLine02 == null || Strings.isNullOrEmpty(descriptionLine02.getValue())) {
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
