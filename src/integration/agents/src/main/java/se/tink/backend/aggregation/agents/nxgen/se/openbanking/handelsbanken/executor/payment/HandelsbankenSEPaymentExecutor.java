package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.AgentIdentificationType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.PaymentAccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class HandelsbankenSEPaymentExecutor extends HandelsbankenBasePaymentExecutor {

    private static final GenericTypeMapper<HandelsbankenPaymentType, Type> PAYMENT_TYPE_MAPPER =
            GenericTypeMapper.<HandelsbankenPaymentType, Type>genericBuilder()
                    .put(HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER, Type.SE)
                    .put(
                            HandelsbankenPaymentType.SWEDISH_DOMESTIC_GIRO_PAYMENT,
                            Type.SE_BG,
                            Type.SE_PG)
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
    protected AccountEntity getDebtorAccountEntity(Payment payment) {
        final Debtor debtor = payment.getDebtor();
        Preconditions.checkArgument(debtor.getAccountIdentifierType().equals(Type.SE));
        // Debtor account without clearing number
        final SwedishIdentifier identifier = new SwedishIdentifier(debtor.getAccountNumber());
        final AccountEntity accountEntity =
                new AccountEntity(identifier.getAccountNumber(), PaymentAccountType.BBAN);
        accountEntity.setText(Strings.emptyToNull(payment.getReference().getValue()));
        return accountEntity;
    }

    @Override
    protected AccountEntity getCreditorAccountEntity(Creditor creditor) {
        final Type creditorType = creditor.getAccountIdentifierType();
        switch (creditorType) {
            case SE:
                // get account number without clearing number
                return new AccountEntity(
                        new SwedishIdentifier(creditor.getAccountNumber()).getAccountNumber(),
                        PaymentAccountType.BBAN);
            case SE_PG:
                return new AccountEntity(
                        new PlusGiroIdentifier(creditor.getAccountNumber()).getGiroNumber(),
                        PaymentAccountType.PLUSGIRO);
            case SE_BG:
                return new AccountEntity(
                        new BankGiroIdentifier(creditor.getAccountNumber()).getGiroNumber(),
                        PaymentAccountType.BANKGIRO);
            default:
                // This should be handled in getPaymentType
                throw new IllegalStateException("Unexpected creditor type: " + creditorType);
        }
    }

    @Override
    protected Optional<CreditorAgentEntity> getCreditorAgentEntity(Creditor creditor) {
        final Type creditorType = creditor.getAccountIdentifierType();
        if (creditorType.equals(Type.SE)) {
            final SwedishIdentifier identifier = new SwedishIdentifier(creditor.getAccountNumber());
            return Optional.of(
                    CreditorAgentEntity.ofIdentification(
                            identifier.getClearingNumber(), AgentIdentificationType.SESBA));
        }

        return Optional.empty();
    }

    @Override
    protected RemittanceInformationEntity getRemittanceInformationEntity(Payment payment) {
        final Creditor creditor = payment.getCreditor();
        final Type creditorType = creditor.getAccountIdentifierType();
        final GiroIdentifier giroIdentifier;
        switch (creditorType) {
            case SE_PG:
                giroIdentifier = new PlusGiroIdentifier(creditor.getAccountNumber());
                break;
            case SE_BG:
                giroIdentifier = new BankGiroIdentifier(creditor.getAccountNumber());
                break;
            default:
                giroIdentifier = null;
        }

        final String text = Strings.emptyToNull(payment.getReference().getValue());
        if (Objects.isNull(giroIdentifier)) {
            return new RemittanceInformationEntity(text);
        } else {
            return new RemittanceInformationEntity(giroIdentifier.getOcr().orElse(text));
        }
    }

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest)
            throws PaymentException {
        final Type debtorType = paymentRequest.getPayment().getDebtor().getAccountIdentifierType();
        if (!debtorType.equals(Type.SE)) {
            final String errorMessage = "Unsupported debtor account type " + debtorType;
            throw new DebtorValidationException(
                    errorMessage, "", new IllegalArgumentException(errorMessage));
        }

        final Type creditorType =
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType();
        return PAYMENT_TYPE_MAPPER
                .translate(creditorType)
                .orElseThrow(
                        () -> {
                            final String errorMessage =
                                    "Unsupported creditor account type " + creditorType;
                            return new CreditorValidationException(
                                    errorMessage, "", new IllegalArgumentException(errorMessage));
                        });
    }

    @Override
    public Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, bankIdSigner);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        final String ssn = credentials.getField(CredentialKeys.USERNAME);
        final SessionResponse sessionResponse =
                apiClient.initDecoupledAuthorizationPis(
                        ssn, paymentMultiStepRequest.getPayment().getUniqueId());
        bankIdSigner.setAutoStartToken(sessionResponse);
        getSigner().sign(paymentMultiStepRequest);

        return super.sign(paymentMultiStepRequest);
    }
}
