package se.tink.agent.sdk.models.payments.beneficiary;

import lombok.EqualsAndHashCode;
import se.tink.agent.sdk.models.payments.beneficiary.builder.BeneficiaryBuildName;
import se.tink.libraries.account.AccountIdentifier;

@EqualsAndHashCode
public class Beneficiary {
    private final String name;
    private final AccountIdentifier accountIdentifier;

    Beneficiary(String name, AccountIdentifier accountIdentifier) {
        this.name = name;
        this.accountIdentifier = accountIdentifier;
    }

    public String getName() {
        return name;
    }

    public AccountIdentifier getAccountIdentifier() {
        return accountIdentifier;
    }

    public static BeneficiaryBuildName builder() {
        return new BeneficiaryBuilder();
    }
}
