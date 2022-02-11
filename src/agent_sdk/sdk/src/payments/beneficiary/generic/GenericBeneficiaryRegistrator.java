package se.tink.agent.sdk.payments.beneficiary.generic;

import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.payments.beneficiary.steppable_execution.BeneficiarySignFlow;
import se.tink.libraries.account.AccountIdentifier;

public interface GenericBeneficiaryRegistrator {

    /**
     * @param debtorAccountIdentifier The account for which this beneficiary should be registered
     *     for. Not always applicable depending on the Bank API.
     * @param beneficiary The beneficiary to register.
     * @return A result containing a bankReference to be authorized/signed or an error if the
     *     beneficiary registration failed.
     */
    BeneficiaryRegisterResult registerBeneficiary(
            AccountIdentifier debtorAccountIdentifier, Beneficiary beneficiary);

    /** @return A series of steps to authorize/sign the previously registered beneficiary. */
    BeneficiarySignFlow getSignFlow();
}
