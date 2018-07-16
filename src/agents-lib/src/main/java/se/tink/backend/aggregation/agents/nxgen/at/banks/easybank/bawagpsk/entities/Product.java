package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

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

    public IbanIdentifier getIban() {
        return new IbanIdentifier(productID.getFinancialInstitute().getBIC().trim(), productID.getIban().trim());
    }

    public TransactionalAccount toTransactionalAccount(final Amount balance) {
        return TransactionalAccount.builder(productID.getAccountType(), accountNumber, balance)
                .setAccountNumber(accountNumber)
                .addIdentifier(getIban())
                .setHolderName(new HolderName(productID.getAccountOwner().trim()))
                .build();
    }
}
