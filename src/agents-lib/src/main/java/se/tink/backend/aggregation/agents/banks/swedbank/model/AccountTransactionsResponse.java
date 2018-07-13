package se.tink.backend.aggregation.agents.banks.swedbank.model;

import java.util.Optional;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransactionsResponse {
	protected AccountEntity account;
	protected CardAccountEntity cardAccount;
	protected boolean moreTransactionsAvailable;
	protected int numberOfReservedTransactions;
	protected int numberOfTransactions;
	protected List<TransactionEntity> reservedTransactions;
	protected List<TransactionEntity> transactions;
	private LinksEntity links;

	public AccountEntity getAccount() {
		return account;
	}

    public CardAccountEntity getCardAccount() {
        return cardAccount;
    }

    public int getNumberOfReservedTransactions() {
		return numberOfReservedTransactions;
	}

	public int getNumberOfTransactions() {
		return numberOfTransactions;
	}

	public List<TransactionEntity> getReservedTransactions() {
		return reservedTransactions;
	}

	public List<TransactionEntity> getTransactions() {
		return transactions;
	}

	public boolean isMoreTransactionsAvailable() {
		return moreTransactionsAvailable;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public void setCardAccount(CardAccountEntity cardAccount) {
        this.cardAccount = cardAccount;
    }

	public void setMoreTransactionsAvailable(boolean moreTransactionsAvailable) {
		this.moreTransactionsAvailable = moreTransactionsAvailable;
	}

	public void setNumberOfReservedTransactions(int numberOfReservedTransactions) {
		this.numberOfReservedTransactions = numberOfReservedTransactions;
	}

	public void setNumberOfTransactions(int numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}

	public void setReservedTransactions(
			List<TransactionEntity> reservedTransactions) {
		this.reservedTransactions = reservedTransactions;
	}

	public void setTransactions(List<TransactionEntity> transactions) {
		this.transactions = transactions;
	}

	public LinksEntity getLinks() {
		return links;
	}

	public void setLinks(LinksEntity links) {
		this.links = links;
	}

	public Optional<String> getTransactionsURI() {
		String URI = null;

		if (links != null) {
			LinkEntity next = links.getNext();

			if (next != null) {
				URI = next.getUri();
			}
		}

		return Optional.ofNullable(URI);
	}
}
