package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils.getFormattedAmount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusProduct;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.libraries.transfer.rpc.Transfer;

public class BelfiusTransferExecutor implements BankTransferExecutor {

    private final BelfiusSessionStorage belfiusSessionStorage;
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private BelfiusApiClient apiClient;
    private BelfiusSignatureCreator belfiusSignatureCreator;

    private static final AggregationLogger LOGGER =
            new AggregationLogger(BelfiusTransferExecutor.class);

    public BelfiusTransferExecutor(
            BelfiusApiClient apiClient,
            BelfiusSessionStorage belfiusSessionStorage,
            Catalog catalog,
            final SupplementalInformationHelper supplementalInformationHelper,
            final BelfiusSignatureCreator belfiusSignatureCreator) {
        this.catalog = catalog;
        this.apiClient = apiClient;
        this.belfiusSessionStorage = belfiusSessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.belfiusSignatureCreator = belfiusSignatureCreator;
    }

    private static String formatStructuredMessage(String structuredMessage) {
        structuredMessage = structuredMessage.replace("+++", "");
        String[] parts = structuredMessage.split("/");
        String special = Character.toString((char) 92);

        return parts[0] + special + "/" + parts[1] + special + "/" + parts[2];
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        boolean immediateTransfer = immediateTransfer(transfer);
        validateAndSetDueDate(transfer);

        List<Pair<TransactionalAccount, BelfiusProduct>> accountPairedWithProduct =
                getTransactionalAccounts();
        ExactCurrencyAmount sourceAccountBalance =
                getSourceAccount(transfer, accountPairedWithProduct).second.getAvailableBalance();
        boolean ownAccount =
                tryFindAccount(accountPairedWithProduct, transfer.getDestination()).isPresent();

        if (transfer.getMessageType().equals(MessageType.STRUCTURED)) {
            transfer.setDestinationMessage(
                    formatStructuredMessage(transfer.getDestinationMessage()));
        }

        BelfiusPaymentResponse paymentResponse =
                apiClient.executePayment(
                        ownAccount,
                        transfer,
                        createClientSha(transfer),
                        transfer.getMessageType().equals(MessageType.STRUCTURED));

        if (paymentResponse.requireSignOfBeneficiary()) {
            addBeneficiary(transfer, transfer.getMessageType().equals(MessageType.STRUCTURED));
        }

        // If there was an error it might have been fixed by addBeneficiary but the addBeneficiary
        // response
        // does not give enough information. The error could also be a double payment warning.
        // Pressing double payment will give us more information.
        if (paymentResponse.isErrorMessageIdentifier()) {
            SignProtocolResponse signProtocolResponse = apiClient.doublePayment();
            if (signOverWeeklyOrDailyLimit(signProtocolResponse)) {
                signPayments();
            } else if (signProtocolResponse.isError()) {
                LOGGER.warnExtraLong(
                        String.format(
                                "Signing response: %s", signProtocolResponse.getErrorMessage()),
                        BelfiusConstants.Transfer.LOGTAG);
                throw createFailedTransferException(
                        TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED,
                        TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED);
            }
        }

        if (immediateTransfer
                && sourceAccountBalance
                                .getExactValue()
                                .compareTo(BigDecimal.valueOf(transfer.getAmount().doubleValue()))
                        < 0) {
            return Optional.of(
                    catalog.getString(
                            TransferExecutionException.EndUserMessage
                                    .EXCESS_AMOUNT_AWAITING_PROCESSING));
        }
        return Optional.empty();
    }

    private boolean signOverWeeklyOrDailyLimit(SignProtocolResponse signProtocolResponse) {
        return signProtocolResponse.requireSignWeeklyLimit()
                || signProtocolResponse.requireSignDailyLimit();
    }

    private boolean immediateTransfer(Transfer transfer) {
        return transfer.getDueDate() == null;
    }

    private Pair<TransactionalAccount, BelfiusProduct> getSourceAccount(
            Transfer transfer, List<Pair<TransactionalAccount, BelfiusProduct>> accounts) {
        return tryFindAccount(accounts, transfer.getSource())
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                TransferExecutionException.EndUserMessage
                                                        .INVALID_SOURCE
                                                        .getKey()
                                                        .get())
                                        .build());
    }

    private void signPayments() {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response;

        try {
            response =
                    supplementalInformationHelper.waitForTwoStepSignForTransferChallengeResponse(
                            transferSignChallenge.getChallenge(),
                            transferSignChallenge.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    e);
        }

        SignProtocolResponse signProtocolResponse = apiClient.signTransfer(response);

        if (!signProtocolResponse.signOk()) {
            multiSignTransfer(signProtocolResponse);
        }
    }

    private void checkThrowableErrors(SignProtocolResponse signProtocolResponse)
            throws TransferExecutionException {
        if (signProtocolResponse.weeklyCardLimitReached()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }

        if (signProtocolResponse.invalidBeneficiarySign() || signProtocolResponse.signError()) {
            throw createFailedTransferException(
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }
    }

    private void multiSignTransfer(SignProtocolResponse signProtocolResponse)
            throws TransferExecutionException {
        boolean success = false;
        if (signProtocolResponse.signTempError()) {
            try {
                success = doubleSignedPayment();
            } catch (SupplementalInfoException e) {
                throw createFailedTransferException(
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        e);
            }
        }

        if (!success) {
            checkThrowableErrors(signProtocolResponse);
            throw createFailedTransferException(
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }
    }

    private TransferExecutionException createFailedTransferException(
            TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage) {
        return createFailedTransferException(message, endUserMessage, null);
    }

    private TransferExecutionException createFailedTransferException(
            TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage,
            SupplementalInfoException e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(catalog.getString(message))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .setException(e)
                .build();
    }

    private boolean doubleSignedPayment()
            throws SupplementalInfoException, TransferExecutionException {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response =
                supplementalInformationHelper.waitForTwoStepSignForTransferChallengeResponse(
                        transferSignChallenge.getChallenge(), transferSignChallenge.getSignType());
        SignProtocolResponse signProtocolResponse = apiClient.doubleSignTransfer(response);

        if (signProtocolResponse.signOk()) {
            return true;
        } else if (signProtocolResponse.signError()) {
            signProtocolResponse = apiClient.doubleClickPayment();
            response =
                    supplementalInformationHelper.waitForTwoStepSignForTransferChallengeResponse(
                            signProtocolResponse.getChallenge(),
                            signProtocolResponse.getSignType());
            signProtocolResponse = apiClient.doubleSignTransfer(response);
            checkThrowableErrors(signProtocolResponse);
            return signProtocolResponse.signOk();
        }

        return false;
    }

    private void addBeneficiary(Transfer transfer, boolean isStructuredMessage)
            throws TransferExecutionException {
        String response;
        try {
            String name = transfer.getDestination().getName().orElse(null);
            if (name == null) {
                name = supplementalInformationHelper.waitForAddBeneficiaryInput();
            }
            SignProtocolResponse signProtocolResponse =
                    apiClient.addBeneficiary(transfer, isStructuredMessage, name);
            if (signProtocolResponse.getChallenge().isEmpty()
                    || signProtocolResponse.getSignType().isEmpty()) {
                throw createFailedTransferException(
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
            }
            response =
                    supplementalInformationHelper.waitForTwoStepSignForBeneficiaryChallengeResponse(
                            signProtocolResponse.getChallenge(),
                            signProtocolResponse.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    e);
        }

        checkThrowableErrors(apiClient.signBeneficiary(response));
    }

    private void validateAndSetDueDate(Transfer transfer) {
        if (transfer.getDueDate() == null) {
            transfer.setDueDate(
                    Date.from(
                            LocalDate.now()
                                    .atStartOfDay()
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()));
        }

        CountryDateHelper countryDateHelper = new CountryDateHelper(new Locale("nl", "BE"));
        if (!countryDateHelper.isBusinessDay(transfer.getDueDate())) {
            transfer.setDueDate(countryDateHelper.getNextBusinessDay(transfer.getDueDate()));
        }
    }

    private Optional<Pair<TransactionalAccount, BelfiusProduct>> tryFindAccount(
            List<Pair<TransactionalAccount, BelfiusProduct>> accounts,
            AccountIdentifier accountIdentifier) {
        return accounts.stream()
                .filter(
                        accountProductPair ->
                                matchingAccount(accountProductPair.first, accountIdentifier))
                .findFirst();
    }

    private boolean matchingAccount(
            TransactionalAccount accountEntity, AccountIdentifier accountIdentifier) {
        return accountEntity.getIdentifiers().stream()
                .anyMatch(identifier -> identifier.equals(accountIdentifier));
    }

    private List<Pair<TransactionalAccount, BelfiusProduct>> getTransactionalAccounts() {
        BelfiusTransactionalAccountFetcher accountFetcher =
                new BelfiusTransactionalAccountFetcher(apiClient, belfiusSessionStorage);
        return accountFetcher.fetchTransactionalAccountPairedWithBelfiusProduct();
    }

    private String createClientSha(Transfer transfer) {
        return belfiusSignatureCreator.createTransferSignature(
                belfiusSessionStorage.getChallenge(),
                "I" + ((SepaEurIdentifier) (transfer.getSource())).getIban(),
                "I" + ((SepaEurIdentifier) (transfer.getDestination())).getIban(),
                getFormattedAmount(transfer.getAmount()),
                transfer.getAmount().getCurrency());
    }
}
