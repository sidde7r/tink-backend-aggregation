package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href account;
    private Href authoriseTransaction;
    private Href balances;
    private Href download;
    private Href first;
    private Href last;
    private Href next;
    private Href previous;
    private Href scaOAuth;
    private Href scaRedirect;
    private Href scaStatus;
    private Href selectAuthenticationMethod;
    private Href self;
    private Href startAuthorisation;
    private Href startAuthorisationWithAuthenticationMethodSelection;
    private Href startAuthorisationWithProprietaryData;
    private Href startAuthorisationWithPsuAuthentication;
    private Href startAuthorisationWithPsuIdentification;
    private Href startAuthorisationWithTransactionAuthorisation;
    private Href status;
    private Href transactionDetails;
    private Href transactions;
    private Href updateProprietaryData;
    private Href updatePsuAuthentication;
    private Href updatePsuIdentification;

    public Href getAccount() {
        return account;
    }

    public void setAccount(Href account) {
        this.account = account;
    }

    public Href getAuthoriseTransaction() {
        return authoriseTransaction;
    }

    public void setAuthoriseTransaction(Href authoriseTransaction) {
        this.authoriseTransaction = authoriseTransaction;
    }

    public Href getBalances() {
        return balances;
    }

    public void setBalances(Href balances) {
        this.balances = balances;
    }

    public Href getDownload() {
        return download;
    }

    public void setDownload(Href download) {
        this.download = download;
    }

    public Href getFirst() {
        return first;
    }

    public void setFirst(Href first) {
        this.first = first;
    }

    public Href getLast() {
        return last;
    }

    public void setLast(Href last) {
        this.last = last;
    }

    public Href getNext() {
        return next;
    }

    public void setNext(Href next) {
        this.next = next;
    }

    public Href getPrevious() {
        return previous;
    }

    public void setPrevious(Href previous) {
        this.previous = previous;
    }

    public Href getScaOAuth() {
        return scaOAuth;
    }

    public void setScaOAuth(Href scaOAuth) {
        this.scaOAuth = scaOAuth;
    }

    public Href getScaRedirect() {
        return scaRedirect;
    }

    public void setScaRedirect(Href scaRedirect) {
        this.scaRedirect = scaRedirect;
    }

    public Href getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(Href scaStatus) {
        this.scaStatus = scaStatus;
    }

    public Href getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }

    public void setSelectAuthenticationMethod(Href selectAuthenticationMethod) {
        this.selectAuthenticationMethod = selectAuthenticationMethod;
    }

    public Href getSelf() {
        return self;
    }

    public void setSelf(Href self) {
        this.self = self;
    }

    public Href getStartAuthorisation() {
        return startAuthorisation;
    }

    public void setStartAuthorisation(Href startAuthorisation) {
        this.startAuthorisation = startAuthorisation;
    }

    public Href getStartAuthorisationWithAuthenticationMethodSelection() {
        return startAuthorisationWithAuthenticationMethodSelection;
    }

    public void setStartAuthorisationWithAuthenticationMethodSelection(
            Href startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection =
                startAuthorisationWithAuthenticationMethodSelection;
    }

    public Href getStartAuthorisationWithProprietaryData() {
        return startAuthorisationWithProprietaryData;
    }

    public void setStartAuthorisationWithProprietaryData(
            Href startAuthorisationWithProprietaryData) {
        this.startAuthorisationWithProprietaryData = startAuthorisationWithProprietaryData;
    }

    public Href getStartAuthorisationWithPsuAuthentication() {
        return startAuthorisationWithPsuAuthentication;
    }

    public void setStartAuthorisationWithPsuAuthentication(
            Href startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    }

    public Href getStartAuthorisationWithPsuIdentification() {
        return startAuthorisationWithPsuIdentification;
    }

    public void setStartAuthorisationWithPsuIdentification(
            Href startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    }

    public Href getStartAuthorisationWithTransactionAuthorisation() {
        return startAuthorisationWithTransactionAuthorisation;
    }

    public void setStartAuthorisationWithTransactionAuthorisation(
            Href startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation =
                startAuthorisationWithTransactionAuthorisation;
    }

    public Href getStatus() {
        return status;
    }

    public void setStatus(Href status) {
        this.status = status;
    }

    public Href getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(Href transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public Href getTransactions() {
        return transactions;
    }

    public void setTransactions(Href transactions) {
        this.transactions = transactions;
    }

    public Href getUpdateProprietaryData() {
        return updateProprietaryData;
    }

    public void setUpdateProprietaryData(Href updateProprietaryData) {
        this.updateProprietaryData = updateProprietaryData;
    }

    public Href getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }

    public void setUpdatePsuAuthentication(Href updatePsuAuthentication) {
        this.updatePsuAuthentication = updatePsuAuthentication;
    }

    public Href getUpdatePsuIdentification() {
        return updatePsuIdentification;
    }

    public void setUpdatePsuIdentification(Href updatePsuIdentification) {
        this.updatePsuIdentification = updatePsuIdentification;
    }
}
