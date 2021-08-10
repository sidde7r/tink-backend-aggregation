package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Debtor;

@JsonInclude(Include.NON_NULL)
@JsonObject
@Getter
public final class DebtorAccountEntity {

    private final String bban;
    private final String iban;

    public DebtorAccountEntity(Debtor debtor) {
        if (AccountIdentifierType.SE.equals(debtor.getAccountIdentifierType())) {
            bban = debtor.getAccountNumber();
            iban = null;
        } else {
            bban = null;
            iban = debtor.getAccountNumber();
        }
    }
}
