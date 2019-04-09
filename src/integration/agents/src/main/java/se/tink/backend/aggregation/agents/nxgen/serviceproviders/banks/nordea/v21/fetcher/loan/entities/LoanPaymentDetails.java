package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanPaymentDetails {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String date;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String interest;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String expenses;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String total;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String amortisation;

    private boolean pending;

    public String getDate() {
        return date;
    }

    public String getInterest() {
        return interest;
    }

    public String getExpenses() {
        return expenses;
    }

    public String getTotal() {
        return total;
    }

    public Double getAmortization() {
        if (!Strings.isNullOrEmpty(amortisation)) {
            return AgentParsingUtils.parseAmount(amortisation);
        }
        return null;
    }
}
