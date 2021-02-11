package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.executor.payment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBasePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaNoPaymentExecutorSelector extends NordeaBasePaymentExecutor {
    private static final GenericTypeMapper<PaymentType, Pair<Type, Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<PaymentType, Pair<Type, Type>>genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new Pair<>(Type.NO, Type.NO),
                                    new Pair<>(Type.NO, Type.IBAN))
                            .build();

    private final SupplementalInformationController supplementalInformationController;

    public NordeaNoPaymentExecutorSelector(
            NordeaBaseApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        super(apiClient);
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        NordeaBaseConstants.ErrorMessages.PAYMENT_TYPE_NOT_FOUND
                                                + accountIdentifiersKey));
    }

    @Override
    protected Collection<PaymentType> getSupportedPaymentTypes() {
        return accountIdentifiersToPaymentTypeMapper.getMappedTypes();
    }

    @Override
    protected Signer getSigner() {
        return new BankIdSigningController(
                supplementalInformationController, new NordeaNoBankIdSigner(this));
    }
}
