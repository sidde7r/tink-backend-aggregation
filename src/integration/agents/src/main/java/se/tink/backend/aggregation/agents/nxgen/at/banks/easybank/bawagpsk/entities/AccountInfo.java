package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AccountInfo")
public class AccountInfo {
    private String bankCode;
    private String accountNumber;
    private String accountCurrency;
    private ProductID productID;
    private AmountEntity currentBalanceEntity;
    private AmountEntity currentSaldoEntity;
    private AmountEntity disposableBalanceEntity;

    public String getBankCode() {
        return bankCode;
    }

    @XmlElement(name = "BankCode")
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @XmlElement(name = "AccountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    @XmlElement(name = "AccountCurrency")
    public void setAccountCurrency(String accountCurrency) {
        this.accountCurrency = accountCurrency;
    }

    public ProductID getProductID() {
        return productID;
    }

    @XmlElement(name = "ProductId")
    public void setProductID(ProductID productID) {
        this.productID = productID;
    }

    public AmountEntity getCurrentBalanceEntity() {
        return currentBalanceEntity;
    }

    public AmountEntity getCurrentSaldoEntity() {
        return currentSaldoEntity;
    }

    public AmountEntity getDisposableBalanceEntity() {
        return disposableBalanceEntity;
    }

    @XmlElement(name = "CurrentBalance")
    public void setCurrentBalanceEntity(AmountEntity amountEntity) {
        currentBalanceEntity = amountEntity;
    }

    @XmlElement(name = "CurrentSaldo")
    public void setCurrentSaldoEntity(AmountEntity amountEntity) {
        currentSaldoEntity = amountEntity;
    }

    @XmlElement(name = "DisposableBalance")
    public void setDisposableBalanceEntity(AmountEntity amountEntity) {
        disposableBalanceEntity = amountEntity;
    }
}
