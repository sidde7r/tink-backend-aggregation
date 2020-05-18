package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities.*;
import se.tink.backend.aggregation.nxgen.controllers.payment.*;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.rpc.CreatePaymentRequest;
import se.tink.libraries.payment.rpc.Reference;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SocieteGeneralePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private SocieteGeneraleApiClient apiClient;
    private String redirectUrl;
    public SocieteGeneralePaymentExecutor(SocieteGeneraleApiClient apiClient,String redirectUrl){
        this.apiClient=apiClient;
        this.redirectUrl = redirectUrl;
    }
    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return null;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) throws PaymentException {
        return null;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchAccessToken();
        PaymentType type = getPaymentType(paymentRequest);

        Payment payment = paymentRequest.getPayment();
        BeneficiaryEntity beneficiary = BeneficiaryEntity.of(paymentRequest);
        List<CreditTransferTransactionEntity> creditTransferTransaction = CreditTransferTransactionEntity.of(paymentRequest);
        SupplementaryDataEntity supplementaryData = SupplementaryDataEntity.of(paymentRequest,redirectUrl);



        CreatePaymentRequest request =
                new CreatePaymentRequest.Builder().withPaymentInformationId(UUID.randomUUID().toString())
                        .withCreationDateTime(ZonedDateTime.now( ZoneId.of("CET") ).format( DateTimeFormatter.ISO_DATE_TIME ))
                .withNumberOfTransactions(SocieteGeneraleConstants.FormValues.NUMBER_OF_TRANSACTIONS)
                .withInitiatingParty(new PartyIdentificationEntity.Builder().withName(SocieteGeneraleConstants.FormValues.PAYMENT_INITIATION_DEFAULT_NAME).build())
                .withPaymentTypeInformation( new PaymentTypeInformationEntity.Builder().withServiceLevel(type.toString()).build())
                .withDebtorAccount(new DebtorAccountEntity(payment.getDebtor().getAccountNumber()))
                .withBeneficiary(beneficiary)
                .withCreditTransferTransaction(creditTransferTransaction)
                .withSupplementaryData(supplementaryData).build();

        return apiClient
                .createPayment(request)
                .toTinkPaymentResponse(type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentException, AuthenticationException {
        return null;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

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

    private static final GenericTypeMapper<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
            accountIdentifiersToPaymentTypeMapper =
            GenericTypeMapper
                    .<PaymentType, Pair<AccountIdentifier.Type, AccountIdentifier.Type>>
                            genericBuilder()
                    .put(PaymentType.SEPA, new Pair<>(AccountIdentifier.Type.IBAN, AccountIdentifier.Type.IBAN))
                    .build();

}
