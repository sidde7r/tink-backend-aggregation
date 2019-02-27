package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;
import se.tink.backend.aggregation.agents.models.Loan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
	@JsonProperty("AccountId")
	protected String accountId;
	@JsonProperty("AccountName")
	protected String accountName;
	@JsonProperty("AccountNumber")
	protected String accountNumber;
	@JsonProperty("AvailableAmount")
	protected double availableAmount;
	@JsonProperty("Balance")
	protected double balance;
	@JsonProperty("IsDelegateAccount")
	protected boolean isDelegateAccount;
    @JsonProperty("IsLoan")
    protected boolean isLoan;

	public String getAccountId() {
		return accountId;
	}

    public String getAccountName() {
		return accountName;
	}

    public String getAccountNumber() {
		return accountNumber;
	}

	public double getAvailableAmount() {
		return availableAmount;
	}

	public double getBalance() {
		return balance;
	}

	public boolean isDelegateAccount() {
		return isDelegateAccount;
	}

	public boolean isLoan() {
        return isLoan;
    }

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setAvailableAmount(double availableAmount) {
		this.availableAmount = availableAmount;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public void setDelegateAccount(boolean isDelegateAccount) {
		this.isDelegateAccount = isDelegateAccount;
	}

	public void setLoan(boolean isLoan) {
        this.isLoan = isLoan;
    }

	public Account toAccount(boolean isCreditCardAccount) {
	    Account account = new Account();

        account.setBankId(accountId);
        account.setName(accountName);
        account.setAccountNumber(accountNumber);
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches("[0-9]{10}|35(TV|GJ)[0-9]{6}"),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        if (isCreditCardAccount) {
            account.setType(AccountTypes.CREDIT_CARD);
        } else if (isLoan) {
            account.setType(AccountTypes.LOAN);
        } else {
        	AccountTypes type = AgentParsingUtils.guessAccountType(account);

        	if (type == AccountTypes.OTHER) {
        		type = DanskeUtils.ACCOUNT_TYPE_MAPPER
						.translate(accountName)
						.orElse(AccountTypes.OTHER);
			}

            account.setType(type);
        }

        if (account.getType() == AccountTypes.CHECKING) {
        	account.setBalance(availableAmount);
        } else {
        	account.setBalance(balance);
        }

        return account;
	}

    public Loan toLoan() {
        Loan loan = new Loan();

        loan.setBalance(balance);
        loan.setName(accountName);

        return loan;
    }

}
