package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class LoanData {
    private static final AggregationLogger log = new AggregationLogger(
            LoanData.class);

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String localNumber;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String granted;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String balance;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interestTermEnds;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interest;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentAccount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentFrequency;

    public String getLocalNumber() {
        return localNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getGranted() {
        if (!Strings.isNullOrEmpty(granted)) {
            return AgentParsingUtils.parseAmount(granted);
        }
        return null;
    }

    public Double getBalance() {
        if (!Strings.isNullOrEmpty(balance)) {
            return AgentParsingUtils.parseAmount(balance);
        }
        return null;
    }

    public Date getInterestTermEnds() {
        try {
            if (!Strings.isNullOrEmpty(interestTermEnds) && interestTermEnds.length() >= 10) {
                return ThreadSafeDateFormat.FORMATTER_DAILY.parse(interestTermEnds.substring(0, 10));
            }
        } catch (ParseException e) {
            log.warn("Failed to parse end date of interest terms");
        }

        return null;
    }

    public Double getInterest() {
        if (!Strings.isNullOrEmpty(interest)) {
            return AgentParsingUtils.parsePercentageFormInterest(interest);
        }
        return null;
    }

    public String getPaymentAccount() {
        return paymentAccount;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }
}
