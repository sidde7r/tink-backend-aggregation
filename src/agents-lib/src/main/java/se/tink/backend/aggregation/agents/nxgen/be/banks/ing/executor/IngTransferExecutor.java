package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.CountryDateUtils;

public class IngTransferExecutor implements BankTransferExecutor {
    private final IngApiClient apiClient;
    private final LoginResponseEntity loginResponse;
    private final PersistentStorage persistentStorage;

    public IngTransferExecutor(IngApiClient apiClient, PersistentStorage persistentStorage, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.loginResponse = ingHelper.retrieveLoginResponse()
                .orElseThrow(() -> new IllegalStateException(IngConstants.LogMessage.LOGIN_RESPONSE_NOT_FOUND));
    }

    @Override
    public void executeTransfer(Transfer transfer) throws TransferExecutionException {
        validateDueDate(transfer.getDueDate());

        AccountListEntity accounts = apiClient.fetchAccounts(loginResponse).map(AccountsResponse::getAccounts)
                .orElseThrow(() -> new IllegalStateException(IngConstants.LogMessage.TRANSFER_ACCOUNTS_NOT_FOUND));

        AccountEntity sourceAccount = tryFindOwnAccount(accounts, transfer.getSource())
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE.getKey().get())
                        .build());

        // For immediate transfers it is not allowed transfer amount not covered by the balance. Blocked in app.
        if (immediateTransfer(transfer)) {
            validateAmountCoveredByBalance(sourceAccount, transfer.getAmount());
        }

        Optional<AccountEntity> destinationAccount = tryFindOwnAccount(accounts, transfer.getDestination());

        if (destinationAccount.isPresent()) {
            new IngInternalTransferExecutor(apiClient, loginResponse)
                    .executeInternalTransfer(transfer, sourceAccount, destinationAccount.get());
            return;
        }

        new IngExternalTransferExecutor(apiClient, loginResponse, persistentStorage)
                .executeExternalTransfer(transfer, sourceAccount);
    }

    private boolean immediateTransfer(Transfer transfer) {
        return transfer.getDueDate() == null;
    }

    private void validateAmountCoveredByBalance(AccountEntity sourceAccount, Amount amount) {
        if (Amount.inEUR(IngHelper.parseAmountStringToDouble(sourceAccount.getBalance()))
                .isLessThan(amount.doubleValue())) {
            IngTransferHelper.cancelTransfer(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT.getKey().get());
        }
    }

    /**
     * ING allows transfers on holidays if due date has not been set by user, i.e. a direct transfer. But you
     * can't select a non business day when setting a future due date, so it has to be verified that due date
     * is a business day if it has been supplied.
     */
    private void validateDueDate(Date dueDate) {
        if (dueDate == null) {
            return;
        }

        CountryDateUtils belgianDateUtils = CountryDateUtils.getBelgianDateUtils();
        Calendar calendar = belgianDateUtils.getCalendar();
        calendar.setTime(dueDate);

        if (!belgianDateUtils.isBusinessDay(calendar)) {
            IngTransferHelper.cancelTransfer(IngConstants.EndUserMessage.DATE_MUST_BE_BUSINESS_DAY.getKey().get());
        }
    }

    private Optional<AccountEntity> tryFindOwnAccount(AccountListEntity accounts,
            AccountIdentifier accountIdentifier) {
        return accounts.stream()
                .filter(ae -> isOwnAccount(ae, accountIdentifier))
                .findFirst();
    }

    private boolean isOwnAccount(AccountEntity accountEntity, AccountIdentifier accountIdentifier) {
        return Objects.equals(accountEntity.getIbanNumber(), accountIdentifier.getIdentifier());
    }
}
