package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
	private static final AggregationLogger log = new AggregationLogger(AccountEntity.class);

	private String accountNumber;
	private int accountType;
	private String accountTypeDescription;
	private String alias;
	private String amount;
	private String bookingNumber;
	private String branchName;
	private String currencyCode;
	private String debitAccount;
	private String disposableAmount;
	private boolean disposition;
	private String dueDate;
	private String endDate;
	private String fwdKey;
	private String givenName;
	private String gracePeriod;
	private String id;
	private String interest;
	private String interestPayDate;
	private String interestPayout;
	private int messageLengthRecipient;
	private int messageLengthStatement;
	private String messageValidCharactersDisplay;
	private String messageValidCharactersExpression;
	private String monthsPerPeriod;
	private int order;
	// private List<BillEntity> pendingBills;
	private String periodicPayDate;
	private String refundMethod;
	private String startDate;
	private String status;
	private String surname;
	private String tax;
	private String totalRefundPeriods;
	private List<TransactionEntity> transactions;
	// private List<BillEntity> upcomingBills;
	private String upcomingBillsCurrentMonthAmount;
	private String creditLimit;
	private String creditUsed;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public String getAccountTypeDescription() {
		return accountTypeDescription;
	}

	public void setAccountTypeDescription(String accountTypeDescription) {
		this.accountTypeDescription = accountTypeDescription;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBookingNumber() {
		return bookingNumber;
	}

	public void setBookingNumber(String bookingNumber) {
		this.bookingNumber = bookingNumber;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getDebitAccount() {
		return debitAccount;
	}

	public void setDebitAccount(String debitAccount) {
		this.debitAccount = debitAccount;
	}

	public String getDisposableAmount() {
		return disposableAmount;
	}

	public void setDisposableAmount(String disposableAmount) {
		this.disposableAmount = disposableAmount;
	}

	public boolean isDisposition() {
		return disposition;
	}

	public void setDisposition(boolean disposition) {
		this.disposition = disposition;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getFwdKey() {
		return fwdKey;
	}

	public void setFwdKey(String fwdKey) {
		this.fwdKey = fwdKey;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(String gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
	}

	public String getInterestPayDate() {
		return interestPayDate;
	}

	public void setInterestPayDate(String interestPayDate) {
		this.interestPayDate = interestPayDate;
	}

	public String getInterestPayout() {
		return interestPayout;
	}

	public void setInterestPayout(String interestPayout) {
		this.interestPayout = interestPayout;
	}

	public int getMessageLengthRecipient() {
		return messageLengthRecipient;
	}

	public void setMessageLengthRecipient(int messageLengthRecipient) {
		this.messageLengthRecipient = messageLengthRecipient;
	}

	public int getMessageLengthStatement() {
		return messageLengthStatement;
	}

	public void setMessageLengthStatement(int messageLengthStatement) {
		this.messageLengthStatement = messageLengthStatement;
	}

	public String getMessageValidCharactersDisplay() {
		return messageValidCharactersDisplay;
	}

	public void setMessageValidCharactersDisplay(
			String messageValidCharactersDisplay) {
		this.messageValidCharactersDisplay = messageValidCharactersDisplay;
	}

	public String getMessageValidCharactersExpression() {
		return messageValidCharactersExpression;
	}

	public void setMessageValidCharactersExpression(
			String messageValidCharactersExpression) {
		this.messageValidCharactersExpression = messageValidCharactersExpression;
	}

	public String getMonthsPerPeriod() {
		return monthsPerPeriod;
	}

	public void setMonthsPerPeriod(String monthsPerPeriod) {
		this.monthsPerPeriod = monthsPerPeriod;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getPeriodicPayDate() {
		return periodicPayDate;
	}

	public void setPeriodicPayDate(String periodicPayDate) {
		this.periodicPayDate = periodicPayDate;
	}

	public String getRefundMethod() {
		return refundMethod;
	}

	public void setRefundMethod(String refundMethod) {
		this.refundMethod = refundMethod;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getTax() {
		return tax;
	}

	public void setTax(String tax) {
		this.tax = tax;
	}

	public String getTotalRefundPeriods() {
		return totalRefundPeriods;
	}

	public void setTotalRefundPeriods(String totalRefundPeriods) {
		this.totalRefundPeriods = totalRefundPeriods;
	}

	public List<TransactionEntity> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<TransactionEntity> transactions) {
		this.transactions = transactions;
	}

	public String getUpcomingBillsCurrentMonthAmount() {
		return upcomingBillsCurrentMonthAmount;
	}

	public void setUpcomingBillsCurrentMonthAmount(
			String upcomingBillsCurrentMonthAmount) {
		this.upcomingBillsCurrentMonthAmount = upcomingBillsCurrentMonthAmount;
	}

    public String getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(String creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getCreditUsed() {
        return creditUsed;
    }

    public void setCreditUsed(String creditUsed) {
        this.creditUsed = creditUsed;
    }

    @JsonIgnore
    public boolean isInvestment() {
		return accountType == 9;
	}

    public Account toAccount() {
		Account account = new Account();

		if (StringUtils.trimToNull(alias) != null) {
			account.setName(alias);
		} else {
			account.setName(Optional
					.ofNullable(StringUtils.trimToNull(accountTypeDescription)).orElse(""));
		}

		String plainAccountId = accountNumber.replace(".", "").replace("-", "");
		account.setBankId(String.format("%s-%s", plainAccountId, plainAccountId));
		account.setAccountNumber(accountNumber);
		account.putIdentifier(new SwedishIdentifier(accountNumber));
		if (StringUtils.trimToNull(disposableAmount) != null) {
			// Preferable.
			account.setBalance(AgentParsingUtils.parseAmount(disposableAmount)
					- Optional.ofNullable(creditLimit).map(AgentParsingUtils::parseAmount)
					.orElse(0.0));
		} else {
			account.setBalance(AgentParsingUtils.parseAmount(amount));
		}

		Preconditions.checkState(
				Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{11}-[0-9]{11}"),
				"Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

		switch (accountType) {
		case 1:
			account.setType(AccountTypes.CHECKING);
			break;
		case 3:
			account.setType(AccountTypes.CREDIT_CARD);
			break;
		case 2:
		case 8:
			account.setType(AccountTypes.SAVINGS);
			break;
		default:
			account.setType(AccountTypes.CHECKING);
			log.warn("Unknown account type: " + accountType + " ("+ accountTypeDescription + ")");
			break;
		}

		return account;
	}

}
