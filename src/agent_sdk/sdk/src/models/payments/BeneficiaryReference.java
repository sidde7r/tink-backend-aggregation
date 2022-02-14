package se.tink.agent.sdk.models.payments;

import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.libraries.account.AccountIdentifier;

public interface BeneficiaryReference {
    AccountIdentifier getDebtorAccountIdentifier();

    Beneficiary getBeneficiary();

    @Nullable
    default String getBankReference() {
        return this.getBankReference(String.class);
    }

    @Nullable
    <T> T getBankReference(Class<T> referenceType);
}
