package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIds {
    @JsonProperty("accountID")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountNumber;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String bankCode;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String country;

    public String getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getCountry() {
        return country;
    }
}
