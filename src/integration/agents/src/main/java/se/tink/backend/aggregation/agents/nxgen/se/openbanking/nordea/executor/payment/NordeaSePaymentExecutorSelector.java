package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBasePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaSePaymentExecutorSelector extends NordeaBasePaymentExecutor {
    // The key is a pair where the key is debtor account type and value is creditor account type.
    // The mapping follows the instructions in:
    // https://developer.nordeaopenbanking.com/app/documentation?api=Payments%20API%20Domestic%20transfer&version=3.3#payment_types_field_combinations
    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                                    genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.SE),
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.IBAN),
                                    new Pair<>(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_BG),
                                    new Pair<>(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_PG))
                            .build();

    private final SupplementalInformationController supplementalInformationController;

    public NordeaSePaymentExecutorSelector(
            NordeaBaseApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        super(apiClient);
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<AccountIdentifier.Type, AccountIdentifier.Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    @Override
    protected Collection<PaymentType> getSupportedPaymentTypes() {
        return accountIdentifiersToPaymentTypeMapper.getMappedTypes();
    }

    @Override
    protected Signer getSigner() {
        return new BankIdSigningController(
                supplementalInformationController, new NordeaSeBankIdSigner(this));
    }
}
