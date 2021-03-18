package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonObject
@Getter
public class AccountEntity implements TinkCreditorConstructor {

    private String bban;
    private String currency;

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifierType.SE, bban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, bban));
    }
}
