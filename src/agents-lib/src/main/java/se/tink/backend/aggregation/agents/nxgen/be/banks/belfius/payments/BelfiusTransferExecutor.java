package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.date.CountryDateUtils;
import se.tink.libraries.i18n.Catalog;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils.createTransferSignature;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils.getFormattedAmount;

public class BelfiusTransferExecutor implements BankTransferExecutor {

    private final BelfiusSessionStorage belfiusSessionStorage;
    private final CountryDateUtils countryDateUtils;
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private BelfiusApiClient apiClient;
    private static final AggregationLogger LOGGER = new AggregationLogger(BelfiusTransferExecutor.class);

    public BelfiusTransferExecutor(BelfiusApiClient apiClient,
            BelfiusSessionStorage belfiusSessionStorage,
            Catalog catalog,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.catalog = catalog;
        this.apiClient = apiClient;
        this.belfiusSessionStorage = belfiusSessionStorage;
        countryDateUtils = CountryDateUtils.getBelgianDateUtils();
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public static String formatStructuredMessage(String structuredMessage) {
        structuredMessage = structuredMessage.replace("+++", "");
        String[] parts = structuredMessage.split("/");
        String special = Character.toString((char) 92);

        return parts[0] + special + "/" + parts[1] + special + "/" + parts[2];
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        boolean immediateTransfer = immediateTransfer(transfer);
        validateAndSetDueDate(transfer);

        Collection<TransactionalAccount> accounts = getTransactionalAccounts();
        Amount sourceAccountBalance = getSourceAccount(transfer, accounts).getBalance();
        boolean ownAccount = tryFindAccount(accounts, transfer.getDestination()).isPresent();

        if (transfer.getMessageType().equals(MessageType.STRUCTURED)) {
            transfer.setDestinationMessage(formatStructuredMessage(transfer.getDestinationMessage()));
        }

        BelfiusPaymentResponse paymentResponse = apiClient
                .executePayment(ownAccount, transfer, createClientSha(transfer),
                        transfer.getMessageType().equals(MessageType.STRUCTURED));

        if (paymentResponse.requireSignOfBeneficiary()) {
            addBeneficiary(transfer, transfer.getMessageType().equals(MessageType.STRUCTURED));
        }

        // If there was an error it might have been fixed by addBeneficiary but the addBeneficiary response
        // does not give enough information. The error could also be a double payment warning.
        // Pressing double payment will give us more information.
        if (paymentResponse.isErrorMessageIdentifier()) {
            SignProtocolResponse signProtocolResponse = apiClient.doublePayment();
            if (signProtocolResponse.requireSignWeeklyLimit()) {
                signPayments();
            } else if (signProtocolResponse.isError()) {
                LOGGER.warnExtraLong(String.format("Signing response: %s", signProtocolResponse.getErrorMessage()),
                        BelfiusConstants.Transfer.LOGTAG);
                throw createFailedTransferException(TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED,
                        TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED);
            }
        }

        if (immediateTransfer && sourceAccountBalance.isLessThan(transfer.getAmount().doubleValue())) {
            return Optional.of(catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT_AWAITING_PROCESSING));
        }
        return Optional.empty();
    }


    private boolean immediateTransfer(Transfer transfer) {
        return transfer.getDueDate() == null;
    }

    private TransactionalAccount getSourceAccount(Transfer transfer, Collection<TransactionalAccount> accounts) {
        return tryFindAccount(accounts, transfer.getSource())
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE.getKey().get())
                    .build());
    }


    public void signPayments() {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response;

        try {
            response = supplementalInformationHelper.waitForSignForTransferChallengeResponse(
                    transferSignChallenge.getChallenge(),
                    transferSignChallenge.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }

        SignProtocolResponse signProtocolResponse = apiClient.signTransfer(response);

        if (!signProtocolResponse.signOk()) {
            multiSignTransfer(signProtocolResponse);
        }
    }

    public void checkThrowableErrors(SignProtocolResponse signProtocolResponse) throws TransferExecutionException {
        if (signProtocolResponse.weeklyCardLimitReached()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }

        if (signProtocolResponse.invalidBeneficiarySign() || signProtocolResponse.signError()) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }
    }

    public void multiSignTransfer(SignProtocolResponse signProtocolResponse) throws TransferExecutionException {
        boolean success = false;
        if (signProtocolResponse.signTempError()) {
            try {
                success = doubleSignedPayment();
            } catch (SupplementalInfoException e) {
                throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
            }
        }

        if (!success) {
            checkThrowableErrors(signProtocolResponse);
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }
    }

    public TransferExecutionException createFailedTransferException(TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage){
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(catalog.getString(message))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }

    public TransferExecutionException createCancelledTransferException(TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage){
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(catalog.getString(message))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }

    public boolean doubleSignedPayment() throws SupplementalInfoException, TransferExecutionException {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response = supplementalInformationHelper.waitForSignForTransferChallengeResponse(
                transferSignChallenge.getChallenge(),
                transferSignChallenge.getSignType());
        SignProtocolResponse signProtocolResponse = apiClient.doubleSignTransfer(response);

        if (signProtocolResponse.signOk()) {
            return true;
        } else if (signProtocolResponse.signError()) {
            signProtocolResponse = apiClient.doubleClickPayment();
            response = supplementalInformationHelper.waitForSignForTransferChallengeResponse(
                    signProtocolResponse.getChallenge(),
                    signProtocolResponse.getSignType());
            signProtocolResponse = apiClient.doubleSignTransfer(response);
            checkThrowableErrors(signProtocolResponse);
            return signProtocolResponse.signOk();
        }

        return false;
    }

    public void addBeneficiary(Transfer transfer, boolean isStructuredMessage) throws TransferExecutionException {
        String response;
        try {
            String name = transfer.getDestination().getName().orElse(null);
            if (name == null) {
                name = supplementalInformationHelper.waitForAddBeneficiaryInput();
            }
            SignProtocolResponse signProtocolResponse = apiClient.addBeneficiary(transfer, isStructuredMessage, name);
            if (signProtocolResponse.getChallenge().isEmpty() || signProtocolResponse.getSignType().isEmpty()) {
                throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
            }
            response = supplementalInformationHelper.waitForSignForBeneficiaryChallengeResponse(
                    signProtocolResponse.getChallenge(),
                    signProtocolResponse.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }

        checkThrowableErrors(apiClient.signBeneficiary(response));
    }

    public void validateAndSetDueDate(Transfer transfer) {
        if (transfer.getDueDate() == null) {
            transfer.setDueDate(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (!countryDateUtils.isBusinessDay(transfer.getDueDate())) {
            transfer.setDueDate(countryDateUtils.getNextBusinessDay(transfer.getDueDate()));
        }
    }

    private Optional<TransactionalAccount> tryFindAccount(Collection<TransactionalAccount> accounts,
            AccountIdentifier accountIdentifier) {
        return accounts.stream()
                .filter(account -> matchingAccount(account, accountIdentifier))
                .findFirst();
    }

    private boolean matchingAccount(TransactionalAccount accountEntity, AccountIdentifier accountIdentifier) {
        return accountEntity.getIdentifiers().stream().anyMatch(identifier -> identifier.equals(accountIdentifier));
    }

    private Collection<TransactionalAccount> getTransactionalAccounts() {
        BelfiusTransactionalAccountFetcher accountFetcher = new BelfiusTransactionalAccountFetcher(apiClient);
        return accountFetcher.fetchAccounts();
    }

    public String createClientSha(Transfer transfer) {
        return createTransferSignature(belfiusSessionStorage.getChallenge(),
                "I" + ((SepaEurIdentifier) (transfer.getSource())).getIban(),
                "I" + ((SepaEurIdentifier) (transfer.getDestination())).getIban(),
                getFormattedAmount(transfer.getAmount()),
                transfer.getAmount().getCurrency());
    }
}
