package se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountEntity {
    private String iban;
    private String currency;

    public AccountEntity(String iban) {
        this.iban = iban;
    }
}
