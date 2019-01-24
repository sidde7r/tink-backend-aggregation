package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.log.legacy.LogUtils;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class OpBankAccountEntity implements TransactionKeyPaginatorResponse<OpBankTransactionPaginationKey> {
    private static final LogUtils LOG = new LogUtils(OpBankAccountEntity.class);

    private String typeCode;
    private String roleCode;
    private String accountNumber;
    private String bic;
    private String productName;
    private String name;
    private double balance;
    private double balanceNoReservations;
    private Double liquidFunds;
    private double creditLimit;
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
        return Optional.ofNullable(OpBankConstants.TypeCode.ACCOUNT_TYPES_BY_TYPE_CODE.get(typeCode))
                .orElse(AccountTypes.OTHER);
    }

    private String getAccountName() {
        if (StringUtils.trimToNull(name) != null) {
            return name;
        } else if (StringUtils.trimToNull(productName) != null) {
            return productName;
        } else {
            return accountNumber;
        }
    }

    public boolean isFrom(Account account) {
        return account != null && accountNumber != null && accountNumber.equals(account.getAccountNumber());
    }

    public String getTypeCode() {
        return typeCode;
    }

    public OpBankAccountEntity setTypeCode(String typeCode) {
        this.typeCode = typeCode;
        return this;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public OpBankAccountEntity setRoleCode(String roleCode) {
        this.roleCode = roleCode;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public OpBankAccountEntity setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getProductName() {
        return productName;
    }

    public OpBankAccountEntity setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getBic() {
        return bic;
    }

    public OpBankAccountEntity setBic(String bic) {
        this.bic = bic;
        return this;
    }

    public double getBalance() {
        return balance;
    }

    public OpBankAccountEntity setBalance(double balance) {
        this.balance = balance;
        return this;
    }

    public double getBalanceNoReservations() {
        return balanceNoReservations;
    }

    public OpBankAccountEntity setBalanceNoReservations(double balanceNoReservations) {
        this.balanceNoReservations = balanceNoReservations;
        return this;
    }

    public Double getLiquidFunds() {
        return liquidFunds;
    }

    public OpBankAccountEntity setLiquidFunds(Double liquidFunds) {
        this.liquidFunds = liquidFunds;
        return this;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public OpBankAccountEntity setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public OpBankAccountEntity setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public List<OpBankTransactionEntity> getTransactions() {
        return transactions;
    }

    public OpBankAccountEntity setTransactions(List<OpBankTransactionEntity> transactions) {
        this.transactions = transactions;
        return this;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public OpBankAccountEntity setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        return this;
    }

    public String getTimestampPrevious() {
        return timestampPrevious;
    }

    public OpBankAccountEntity setTimestampPrevious(String timestampPrevious) {
        this.timestampPrevious = timestampPrevious;
        return this;
    }

    public String getName() {
        return name;
    }

    public OpBankAccountEntity setName(String name) {
        this.name = name;
        return this;
    }
}
