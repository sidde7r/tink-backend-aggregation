package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class AccountIdentificationEntity {
    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    @JsonCreator
    public AccountIdentificationEntity(
            @JsonProperty("iban") String iban, @JsonProperty("currency") String currency) {
        this.iban = iban;
        this.currency = currency;
    }

    public AccountIdentificationEntity iban(String iban) {
        this.iban = iban;
        return this;
    }
}
