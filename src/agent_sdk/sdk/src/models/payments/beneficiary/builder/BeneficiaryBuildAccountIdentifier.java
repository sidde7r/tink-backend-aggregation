package se.tink.agent.sdk.models.payments.beneficiary.builder;

import se.tink.libraries.account.AccountIdentifier;

public interface BeneficiaryBuildAccountIdentifier {
    BeneficiaryBuild accountIdentifier(AccountIdentifier accountIdentifier);
}
