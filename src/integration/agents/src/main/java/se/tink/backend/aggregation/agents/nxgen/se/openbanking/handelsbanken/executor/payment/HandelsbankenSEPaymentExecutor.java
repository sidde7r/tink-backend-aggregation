package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import java.util.ArrayList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.InitiatePaymentBodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.Creditor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.RemittanceInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.utils.AccountTypePair;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.AccountIdentifier.Type;

public class HandelsbankenSEPaymentExecutor extends HandelsbankenBasePaymentExecutor {

    private static final GenericTypeMapper<HandelsbankenPaymentType, AccountTypePair>
            accountIdentifiersToPaymentProductMapper =
                    GenericTypeMapper.<HandelsbankenPaymentType, AccountTypePair>genericBuilder()
                            .put(
                                    HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER,
                                    new AccountTypePair(Type.SE, Type.SE))
                            .put(
                                    HandelsbankenPaymentType.SWEDISH_DOMESTIC_GIRO_PAYMENT,
                                    new AccountTypePair(Type.SE, Type.SE_BG),
                                    new AccountTypePair(Type.SE, Type.SE_PG))
                            .build();
    private final SupplementalRequester supplementalRequester;
    private Credentials credentials;
    private final HandelsbankenSEBankIdSigner bankIdSigner;

    public HandelsbankenSEPaymentExecutor(
            HandelsbankenBaseApiClient apiClient,
            Credentials credentials,
            SupplementalRequester supplementalRequester,
            PersistentStorage persistentStorage) {
        super(apiClient);
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
        this.bankIdSigner =
                new HandelsbankenSEBankIdSigner(persistentStorage, apiClient, credentials);
    }

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest) {
        // CrossCurreny and Sepa are still not available.

        //        Pair<Type, Type> accountIdentifiersKey =
        //                paymentRequest.getPayment().getCreditorAndDebtorAccountType();
        //
        //        return accountIdentifiersToPaymentProductMapper
        //                .translate(new AccountTypePair(accountIdentifiersKey))
        //                .orElseGet(() -> getSepaOrCrossCurrencyPaymentType(paymentRequest));

        return HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER;
    }

    @Override
    public Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, bankIdSigner);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        final AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        final AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        final RemittanceInformation remittanceInformation =
                new RemittanceInformation(paymentRequest.getPayment().getReference().getValue());
        final Creditor creditorInfo =
                new Creditor(paymentRequest.getPayment().getCreditor().getName());
        final IdentificationEntity identificationEntity =
                new IdentificationEntity(
                        InitiatePaymentBodyValues.IDENTIFICATION_CODE,
                        InitiatePaymentBodyValues.IDENTIFICATION_TYPE);
        final CreditorAgent agent = new CreditorAgent(identificationEntity);

        final CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        creditor, debtor, amount, remittanceInformation, creditorInfo, agent);

        final HandelsbankenPaymentType paymentProduct = getPaymentType(paymentRequest);

        final PaymentResponse paymentResponse =
                apiClient
                        .createPayment(createPaymentRequest, paymentProduct)
                        .toTinkPaymentResponse(paymentRequest.getPayment(), paymentProduct);

        createdPaymentList.add(paymentResponse);

        return paymentResponse;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String ssn = credentials.getField(CredentialKeys.USERNAME);
        SessionResponse sessionResponse =
                apiClient.initDecoupledAuthorizationPis(
                        ssn, paymentMultiStepRequest.getPayment().getUniqueId());
        bankIdSigner.setAutoStartToken(sessionResponse);
        try {
            getSigner().sign(paymentMultiStepRequest);
        } catch (AuthenticationException e) {
            if (e instanceof BankIdException) {
                BankIdError bankIdError = ((BankIdException) e).getError();
                switch (bankIdError) {
                    case CANCELLED:
                        throw new PaymentAuthorizationException(
                                "BankId signing cancelled by the user.", e);

                    case NO_CLIENT:
                        throw new PaymentAuthorizationException(
                                "No BankId client when trying to sign the payment.", e);

                    case TIMEOUT:
                        throw new PaymentAuthorizationException("BankId signing timed out.", e);

                    case INTERRUPTED:
                        throw new PaymentAuthorizationException("BankId signing interrupded.", e);

                    case UNKNOWN:
                    default:
                        throw new PaymentAuthorizationException(
                                "Unknown problem when signing payment with BankId.", e);
                }
            }
        }
        final ConfirmPaymentResponse confirmPaymentResponse =
                apiClient.confirmPayment(
                        paymentMultiStepRequest.getPayment().getUniqueId(),
                        HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER);

        return new PaymentMultiStepResponse(
                confirmPaymentResponse.toTinkPaymentResponse(
                        paymentMultiStepRequest.getPayment(),
                        HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER),
                SigningStepConstants.STEP_FINALIZE,
                new ArrayList<>());
    }
}
