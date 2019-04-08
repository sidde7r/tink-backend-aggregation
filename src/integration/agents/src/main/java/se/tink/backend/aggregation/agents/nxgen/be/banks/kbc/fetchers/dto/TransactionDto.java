package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionDto {
    private TypeValuePair transactionDate;
    private TypeValuePair amount;
    private TypeEncValueTuple appendixType;
    private TypeValuePair descriptionLine01;
    private TypeValuePair descriptionLine02;
    private TypeEncValueTuple subAccountNo;
    private TypeEncValueTuple registrationTs;
    private TypeEncValueTuple bookingDate;

    public TypeValuePair getTransactionDate() {
        return transactionDate;
    }

    public TypeValuePair getAmount() {
        return amount;
    }

    public TypeEncValueTuple getAppendixType() {
        return appendixType;
    }

    public TypeValuePair getDescriptionLine01() {
        return descriptionLine01;
    }

    public TypeValuePair getDescriptionLine02() {
        return descriptionLine02;
    }

    public TypeEncValueTuple getSubAccountNo() {
        return subAccountNo;
    }

    public TypeEncValueTuple getRegistrationTs() {
        return registrationTs;
    }

    public TypeEncValueTuple getBookingDate() {
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
