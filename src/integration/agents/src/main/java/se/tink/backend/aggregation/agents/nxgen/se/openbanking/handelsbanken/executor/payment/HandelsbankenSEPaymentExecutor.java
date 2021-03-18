package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CreditorAgentIdentificationType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.PaymentAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.PaymentValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class HandelsbankenSEPaymentExecutor extends HandelsbankenBasePaymentExecutor {

    private static final GenericTypeMapper<HandelsbankenPaymentType, AccountIdentifierType>
            PAYMENT_TYPE_MAPPER =
                    GenericTypeMapper
                            .<HandelsbankenPaymentType, AccountIdentifierType>genericBuilder()
                            .put(
                                    HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER,
                                    AccountIdentifierType.SE)
                            .put(
                                    HandelsbankenPaymentType.SWEDISH_DOMESTIC_GIRO_PAYMENT,
                                    AccountIdentifierType.SE_BG,
                                    AccountIdentifierType.SE_PG)
                            .build();
    private final SupplementalInformationController supplementalInformationController;
    private Credentials credentials;
    private final HandelsbankenSEBankIdSigner bankIdSigner;

    public HandelsbankenSEPaymentExecutor(
            HandelsbankenBaseApiClient apiClient,
            Credentials credentials,
            SupplementalInformationController supplementalInformationController,
            PersistentStorage persistentStorage) {
        super(apiClient);
        this.credentials = credentials;
        this.supplementalInformationController = supplementalInformationController;
        this.bankIdSigner =
                new HandelsbankenSEBankIdSigner(persistentStorage, apiClient, credentials);
    }

    @Override
    protected AccountEntity getDebtorAccountEntity(Payment payment) {
        final Debtor debtor = payment.getDebtor();
        Preconditions.checkArgument(
                debtor.getAccountIdentifierType().equals(AccountIdentifierType.SE));
        // Debtor account without clearing number
        final SwedishIdentifier identifier = new SwedishIdentifier(debtor.getAccountNumber());
        final AccountEntity accountEntity =
                new AccountEntity(identifier.getAccountNumber(), PaymentAccountType.BBAN);
        accountEntity.setText(Strings.emptyToNull(payment.getRemittanceInformation().getValue()));
        return accountEntity;
    }

    @Override
    protected AccountEntity getCreditorAccountEntity(Creditor creditor) {
        final AccountIdentifierType creditorType = creditor.getAccountIdentifierType();
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
        final AccountIdentifierType creditorType = creditor.getAccountIdentifierType();
        if (creditorType.equals(AccountIdentifierType.SE)) {
            final SwedishIdentifier identifier = new SwedishIdentifier(creditor.getAccountNumber());
            return Optional.of(
                    CreditorAgentEntity.ofIdentification(
                            identifier.getClearingNumber(),
                            CreditorAgentIdentificationType.SE_CLEARING_NUMBER));
        }

        return Optional.empty();
    }

    @Override
    protected RemittanceInformationEntity getRemittanceInformationEntity(Payment payment) {
        final Creditor creditor = payment.getCreditor();
        final AccountIdentifierType creditorType = creditor.getAccountIdentifierType();
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
        // SHB's docs have a generic field remittanceInformation/text
        // which is used for both OCR and message
        // no field type required
        // so our RemittanceInfoTypes (OCR, UNSTRUCTURED), would only be helpful
        // to check if intended OCR is a valid OCR, which we are doing at paymentService level

        final String text = Strings.emptyToNull(payment.getRemittanceInformation().getValue());
        if (Objects.isNull(giroIdentifier)) {
            return new RemittanceInformationEntity(text);
        } else {
            return new RemittanceInformationEntity(giroIdentifier.getOcr().orElse(text));
        }
    }

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest)
            throws PaymentException {
        final AccountIdentifierType debtorType =
                paymentRequest.getPayment().getDebtor().getAccountIdentifierType();
        if (!debtorType.equals(AccountIdentifierType.SE)) {
            final String errorMessage = "Unsupported debtor account type " + debtorType;
            throw new DebtorValidationException(
                    errorMessage, "", new IllegalArgumentException(errorMessage));
        }

        final AccountIdentifierType creditorType =
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType();
        return PAYMENT_TYPE_MAPPER
                .translate(creditorType)
                .orElseThrow(() -> createCreditorTypeValidationException(creditorType));
    }

    private CreditorValidationException createCreditorTypeValidationException(
            AccountIdentifierType creditorType) {
        final String errorMessage = "Unsupported creditor account type " + creditorType;
        return new CreditorValidationException(
                errorMessage, "", new IllegalArgumentException(errorMessage));
    }

    @Override
    public Signer getSigner() {
        return new BankIdSigningController<PaymentMultiStepResponse>(
                supplementalInformationController, bankIdSigner);
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

    private int getRemittanceInformationMaxLength(
            HandelsbankenPaymentType paymentType, Creditor creditor) {
        switch (paymentType) {
            case SWEDISH_DOMESTIC_GIRO_PAYMENT:
                return PaymentValue.MAX_DEST_MSG_LEN_GIRO;
            case SWEDISH_DOMESTIC_CREDIT_TRANSFER:
                final Bank creditorBank =
                        new SwedishIdentifier(creditor.getAccountNumber()).getBank();
                if (creditorBank == Bank.HANDELSBANKEN) {
                    return PaymentValue.MAX_DEST_MSG_LEN_DOMESTIC_SHB;
                } else {
                    return PaymentValue.MAX_DEST_MSG_LEN_DOMESTIC;
                }
            default:
                throw new IllegalStateException(
                        "Unsupported payment type " + paymentType.toString());
        }
    }

    @Override
    protected void validateRemittanceInformation(
            HandelsbankenPaymentType paymentType, Payment payment)
            throws ReferenceValidationException {

        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation,
                null,
                RemittanceInformationType.UNSTRUCTURED,
                RemittanceInformationType.OCR);

        final String text = Strings.nullToEmpty(payment.getRemittanceInformation().getValue());
        int maxLength = getRemittanceInformationMaxLength(paymentType, payment.getCreditor());

        if (text.length() > maxLength) {
            throw new ReferenceValidationException(
                    String.format(ExceptionMessages.PAYMENT_REF_TOO_LONG, maxLength),
                    "",
                    new IllegalArgumentException());
        }
    }

    @Override
    protected void validateCreditor(Creditor creditor) throws CreditorValidationException {
        if (Strings.nullToEmpty(creditor.getName()).length()
                > PaymentValue.MAX_CREDITOR_NAME_LENGTH) {
            throw new CreditorValidationException(
                    String.format(
                            ExceptionMessages.PAYMENT_CREDITOR_NAME_TOO_LONG,
                            PaymentValue.MAX_CREDITOR_NAME_LENGTH),
                    "",
                    new IllegalArgumentException());
        }
    }
}
