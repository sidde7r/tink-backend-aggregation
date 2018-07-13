package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListAccountTransactionRequest {
    protected String accountNumber;
    protected int requestedPage;

    public ListAccountTransactionRequest(int requestedPage, String accountNumber) {
        this.requestedPage = requestedPage;
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getRequestedPage() {
        return requestedPage;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setRequestedPage(int requestedPage) {
        this.requestedPage = requestedPage;
    }
}
