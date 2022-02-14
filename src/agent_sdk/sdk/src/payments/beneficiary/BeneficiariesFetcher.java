package se.tink.agent.sdk.payments.beneficiary;

import java.util.List;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.libraries.account.AccountIdentifier;

public interface BeneficiariesFetcher {
    /**
     * @param debtorAccountIdentifier The account for which beneficiaries to fetch. Not always
     *     applicable as some Banks have a global list of beneficiaries.
     * @return A list of registered beneficiaries for the debtorAccountIdentifier.
     */
    List<Beneficiary> fetchPaymentBeneficiariesFor(AccountIdentifier debtorAccountIdentifier);
}
