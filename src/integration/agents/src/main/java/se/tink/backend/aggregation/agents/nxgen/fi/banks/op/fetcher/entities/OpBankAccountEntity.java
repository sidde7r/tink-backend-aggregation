package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class OpBankAccountEntity implements TransactionKeyPaginatorResponse<OpBankTransactionPaginationKey> {

    // Account related fields
    private String type;
    private String accountNumber;
    private String encryptedAccountNumber;
    private String bankingServiceTypeCode;
    private String versionNumber;
    private String productName;
    private String accountNameGivenByUser;
    private double amountAvailable;
    private String ownerName;
    private boolean mainAccountOfWebServiceAgreement;
    private double balance;
    private double netBalance;
    private double lineOfCredit;
    private double blocking;
    private String roleCode;
    private String startDateOfAuthorization;

    // Transaction related fields
    private List<OpBankTransactionEntity> transactions;
    private String startDate;
    private boolean hasMore;
    private String timestampPrevious;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        return transactions
                .stream()
                .map(OpBankTransactionEntity::toTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(hasMore);
    }

    // this is a way to handle the need for two keys when paginating for OPBanken
    // if this is a common scenario we should have a different paginator to handle this
    @Override
    public OpBankTransactionPaginationKey nextKey() {
        return new OpBankTransactionPaginationKey(startDate, timestampPrevious);
    }

    /*
     * Convert OpBank account entity to a Tink account.
     * Currently there are no identifiers set for the tink account.
     * This is because we have temporarily decided to not use identifiers for non-swedish bank-agents.
     */
    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), accountNumber, Amount.inEUR(balance))
                .setAccountNumber(accountNumber)
                .setName(getAccountName())
                .setBankIdentifier(accountNumber)
                .build();
    }

    public AccountTypes getTinkAccountType() {
        return Optional.ofNullable(OpBankConstants.TypeCode.ACCOUNT_TYPES_BY_TYPE_CODE.get(bankingServiceTypeCode))
                .orElse(AccountTypes.OTHER);
    }

    private String getAccountName() {

        if (!Strings.isNullOrEmpty(accountNameGivenByUser)) {
            return accountNameGivenByUser;
        }

        if (!Strings.isNullOrEmpty(productName)) {
            return productName;
        }

        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public OpBankAccountEntity setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getEncryptedAccountNumber() {
        return encryptedAccountNumber;
    }

    public OpBankAccountEntity setBankingServiceTypeCode(String bankingServiceTypeCode) {
        this.bankingServiceTypeCode = bankingServiceTypeCode;
        return this;
    }

    public String getBankingServiceTypeCode() {
        return bankingServiceTypeCode;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public String getProductName() {
        return productName;
    }

    public String getAccountNameGivenByUser() {
        return accountNameGivenByUser;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isMainAccountOfWebServiceAgreement() {
        return mainAccountOfWebServiceAgreement;
    }

    public double getBalance() {
        return balance;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public double getLineOfCredit() {
        return lineOfCredit;
    }

    public double getBlocking() {
        return blocking;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getStartDateOfAuthorization() {
        return startDateOfAuthorization;
    }

    public List<OpBankTransactionEntity> getTransactions() {
        return transactions;
    }

    public String getStartDate() {
        return startDate;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public String getTimestampPrevious() {
        return timestampPrevious;
    }
}
