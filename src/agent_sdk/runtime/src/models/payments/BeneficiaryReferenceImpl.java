package se.tink.agent.runtime.models.payments;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.agent.sdk.models.payments.BeneficiaryReference;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.storage.Reference;

@AllArgsConstructor
public class BeneficiaryReferenceImpl implements BeneficiaryReference {
    private final Beneficiary beneficiary;
    @Nullable private final Reference bankReference;

    @Override
    public Beneficiary getBeneficiary() {
        return this.beneficiary;
    }

    @Nullable
    @Override
    public <T> T getBankReference(Class<T> referenceType) {
        return Optional.ofNullable(this.bankReference)
                .flatMap(x -> x.tryGet(referenceType))
                .orElse(null);
    }
}
