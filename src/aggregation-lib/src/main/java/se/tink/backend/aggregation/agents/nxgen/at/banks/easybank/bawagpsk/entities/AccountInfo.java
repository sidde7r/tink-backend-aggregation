package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AccountInfo")
public class AccountInfo {
    private String bankCode;
    private String accountNumber;
    private String accountCurrency;
    private ProductID productID;
    private AmountEntity amountEntity;

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

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    @XmlElement(name = "CurrentBalance")
    public void setAmountEntity(AmountEntity amountEntity) {
        this.amountEntity = amountEntity;
    }
}
