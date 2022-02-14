package se.tink.agent.runtime.models.payments;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.agent.sdk.models.payments.BeneficiaryReference;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.storage.Reference;
import se.tink.libraries.account.AccountIdentifier;

@AllArgsConstructor
public class BeneficiaryReferenceImpl implements BeneficiaryReference {
    private final AccountIdentifier debtorAccountIdentifier;
    private final Beneficiary beneficiary;
    @Nullable private final Reference bankReference;

    @Override
    public AccountIdentifier getDebtorAccountIdentifier() {
        return this.debtorAccountIdentifier;
    }

    @Override
    public Beneficiary getBeneficiary() {
        return this.beneficiary;
    }

    @Nullable
    @Override
    public <T> T getBankReference(Class<T> referenceType) {
        return Optional.ofNullable(this.bankReference)
                .flatMap(reference -> reference.tryGet(referenceType))
                .orElse(null);
    }
}
