package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.DebtorCreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc.PaymentSetupV11Request;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc.PaymentSetupV11Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc.PaymentSubmissionV11Request;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc.PaymentSubmissionV11Response;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.amount.Amount;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class UkOpenBankingV11Pis implements UkOpenBankingPis {
    private final String internalTransferId;
    private final String externalTransferId;

    private final boolean mustNotHaveSourceAccountSpecified;

    public UkOpenBankingV11Pis() {
        this(false);
    }

    public UkOpenBankingV11Pis(boolean mustNotHaveSourceAccountSpecified) {
        this.mustNotHaveSourceAccountSpecified = mustNotHaveSourceAccountSpecified;

        this.internalTransferId = RandomUtils.generateRandomHexEncoded(8);
        this.externalTransferId = RandomUtils.generateRandomHexEncoded(8);
    }

    private Optional<DebtorCreditorAccountEntity> convertAccountIdentifierToUkOpenBanking(
            AccountIdentifier accountIdentifier) throws TransferExecutionException {

        if (Objects.isNull(accountIdentifier)) {
            return Optional.empty();
        }

        switch (accountIdentifier.getType()) {
        case SORT_CODE:
            return Optional.of(
                    DebtorCreditorAccountEntity.createSortCodeAccount(
                        accountIdentifier.getIdentifier(),
                        accountIdentifier.getName().orElse(null)
                    )
            );
        case IBAN:
            IbanIdentifier ibanIdentifier = (IbanIdentifier) accountIdentifier;
            return Optional.of(
                    DebtorCreditorAccountEntity.createIbanAccount(
                            ibanIdentifier.getIban(),
                            accountIdentifier.getName().orElse(null)
                    )
            );
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Unknown identifier type: %s", accountIdentifier.getType()))
                    .build();
        }
    }

    @Override
    public boolean mustNotHaveSourceAccountSpecified() {
        return mustNotHaveSourceAccountSpecified;
    }

    @Override
    public String getBankTransferIntentId(UkOpenBankingApiClient apiClient,
            @Nullable AccountIdentifier sourceIdentifier, AccountIdentifier destinationIdentifier, Amount amount,
            String referenceText) throws TransferExecutionException {

        if (!UkOpenBankingV11Constants.PIS_CURRENCY.equalsIgnoreCase(amount.getCurrency())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            String.format(
                                    "Invalid currency! Expected: '%s' but found: '%s'.",
                                    UkOpenBankingV11Constants.PIS_CURRENCY,
                                    amount.getCurrency()
                            )
                    )
                    .build();
        }

        Optional<DebtorCreditorAccountEntity> ukOpenBankingSourceAccount = convertAccountIdentifierToUkOpenBanking(
                sourceIdentifier);

        DebtorCreditorAccountEntity ukOpenBankingDestinationAccount = convertAccountIdentifierToUkOpenBanking(
                destinationIdentifier)
                .orElseThrow(() ->
                        TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setMessage(
                                        TransferExecutionException.EndUserMessage.INVALID_DESTINATION.getKey().get())
                                .build()
                );

        PaymentSetupV11Request request = PaymentSetupV11Request.createPersonToPerson(
                internalTransferId,
                externalTransferId,
                ukOpenBankingSourceAccount.orElse(null),
                ukOpenBankingDestinationAccount,
                amount,
                referenceText
        );

        PaymentSetupV11Response paymentSetupResponse = apiClient.createPaymentIntentId(request,
                PaymentSetupV11Response.class);

        UkOpenBankingConstants.TransactionIndividualStatus1Code receivedStatus = paymentSetupResponse.getStatus()
                .orElseThrow(() ->
                        TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setMessage("No setup status.")
                                .build()
                );

        switch (receivedStatus) {
        case PENDING:
        case ACCEPTED_TECHNICAL_VALIDATION:
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            String.format(
                                    "Invalid setup status: %s.",
                                    receivedStatus.toValue()
                            )
                    )
                    .build();
        }

        return paymentSetupResponse.getIntentId()
                .orElseThrow(
                        () -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setMessage("No intentId.")
                                .build()
                );
    }

    @Override
    public void executeBankTransfer(
            UkOpenBankingApiClient apiClient,
            String intentId,
            AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            Amount amount,
            String referenceText) throws TransferExecutionException {

        Optional<DebtorCreditorAccountEntity> ukOpenBankingSourceAccount =
                convertAccountIdentifierToUkOpenBanking(sourceIdentifier);

        DebtorCreditorAccountEntity ukOpenBankingDestinationAccount =
                convertAccountIdentifierToUkOpenBanking(destinationIdentifier)
                        .orElseThrow(() ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                TransferExecutionException.EndUserMessage.INVALID_DESTINATION
                                                        .getKey()
                                                        .get()
                                        )
                                        .build()
                        );

        PaymentSubmissionV11Request paymentSubmissionRequest = PaymentSubmissionV11Request.createPersonToPerson(
                intentId,
                internalTransferId,
                externalTransferId,
                ukOpenBankingSourceAccount.orElse(null),
                ukOpenBankingDestinationAccount,
                amount,
                referenceText
        );

        PaymentSubmissionV11Response paymentSubmissionResponse = apiClient.submitPayment(
                paymentSubmissionRequest, PaymentSubmissionV11Response.class);

        UkOpenBankingConstants.TransactionIndividualStatus1Code receivedStatus =
                paymentSubmissionResponse.getStatus()
                        .orElseThrow(() ->
                            TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                    .setMessage("No submission status.")
                                    .build()
                        );

        switch (receivedStatus) {
        case ACCEPTED_SETTLEMENT_IN_PROCESS:
        case ACCEPTED_SETTLEMENT_COMPLETED:
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            String.format(
                                    "Invalid submission status: %s.",
                                    receivedStatus.toValue()
                            )
                    )
                    .build();
        }
    }
}
