package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class GirosCreditorAccountEntity implements TinkCreditorConstructor {

    private String number;

    @JsonIgnore private Type type;

    @Override
    public Creditor toTinkCreditor() {
        return new Creditor(AccountIdentifier.create(type, number));
    }
}
