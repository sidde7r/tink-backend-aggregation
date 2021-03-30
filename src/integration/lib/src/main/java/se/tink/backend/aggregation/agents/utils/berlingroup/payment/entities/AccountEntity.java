package se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity {
    private String iban;

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
