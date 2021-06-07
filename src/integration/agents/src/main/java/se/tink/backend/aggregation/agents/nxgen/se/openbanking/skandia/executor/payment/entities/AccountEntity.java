package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@EqualsAndHashCode
@JsonObject
@Getter
public class AccountEntity implements TinkCreditorConstructor {

    private final String bban;

    @JsonCreator
    public AccountEntity(@JsonProperty("bban") String bban) {
        this.bban = bban;
    }

    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(AccountIdentifierType.SE, bban));
    }

    public Debtor toTinkDebtor() {
        return new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, bban));
    }
}
