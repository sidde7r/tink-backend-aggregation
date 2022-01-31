package se.tink.agent.sdk.models.payments.beneficiary;

import com.google.common.base.Preconditions;
import se.tink.agent.sdk.models.payments.beneficiary.builder.BeneficiaryBuild;
import se.tink.agent.sdk.models.payments.beneficiary.builder.BeneficiaryBuildAccountIdentifier;
import se.tink.agent.sdk.models.payments.beneficiary.builder.BeneficiaryBuildName;
import se.tink.libraries.account.AccountIdentifier;

public class BeneficiaryBuilder
        implements BeneficiaryBuildName, BeneficiaryBuildAccountIdentifier, BeneficiaryBuild {

    private String name;
    private AccountIdentifier accountIdentifier;

    BeneficiaryBuilder() {}

    @Override
    public BeneficiaryBuildAccountIdentifier name(String name) {
        this.name = Preconditions.checkNotNull(name);
        return this;
    }

    @Override
    public BeneficiaryBuild accountIdentifier(AccountIdentifier accountIdentifier) {
        this.accountIdentifier = Preconditions.checkNotNull(accountIdentifier);
        return this;
    }

    @Override
    public Beneficiary build() {
        return new Beneficiary(this.name, this.accountIdentifier);
    }
}
