package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Product")
public class Product {
    private String bankCode;
    private String accountNumber;
    private String accountCurrency;
    private ProductID productID;

    @XmlElement(name = "BankCode")
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankCode() {
        return bankCode;
    }

    @XmlElement(name = "AccountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @XmlElement(name = "AccountCurrency")
    public void setAccountCurrency(String accountCurrency) {
        this.accountCurrency = accountCurrency;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    @XmlElement(name = "ProductId")
    public void setProductID(ProductID productID) {
        this.productID = productID;
    }

    public ProductID getProductID() {
        return productID;
    }
}
