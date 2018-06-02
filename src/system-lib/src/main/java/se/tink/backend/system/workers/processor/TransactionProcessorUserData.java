package se.tink.backend.system.workers.processor;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableListMultimap;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Transaction;

import com.google.common.collect.Maps;

public class TransactionProcessorUserData {
	private List<Account> accounts;
	private List<Credentials> credentials;
	private HashMap<String, Transaction> inStoreTransactions = Maps.newHashMap();
    private ImmutableListMultimap<String, Loan> loanDataByAccount;

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	public List<Credentials> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<Credentials> credentials) {
		this.credentials = credentials;
	}

	public HashMap<String, Transaction> getInStoreTransactions() {
		return inStoreTransactions;
	}

	public void setInStoreTransactions(List<Transaction> transactions) {
		for (Transaction t : transactions) {
            inStoreTransactions.put(t.getId(), t);
        }
	}
	
	public Transaction getInStoreTransaction(String id) {
	    return inStoreTransactions.get(id);
	}

    public ImmutableListMultimap<String, Loan> getLoanDataByAccount() {
        return loanDataByAccount;
    }

    public void setLoanDataByAccount(ImmutableListMultimap<String, Loan> loanDataByAccount) {
        this.loanDataByAccount = loanDataByAccount;
    }
}
