package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "ProductID")
@XmlType(
        propOrder = {
            "productType",
            "productCode",
            "productDescription",
            "accountNumber",
            "iban",
            "financialInstitute",
            "accountOwner"
        })
public class ProductID {
    private String productType;
    private String productCode;
    private String productDescription;
    private String accountNumber;
    private String iban;
    private FinancialInstitute financialInstitute;
    private String accountOwner;

    private static final Logger logger = LoggerFactory.getLogger(ProductID.class);

    @XmlElement(name = "ProductType")
    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductType() {
        return productType;
    }

    @XmlElement(name = "ProductDescription")
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductDescription() {
        return productDescription;
    }

    @XmlElement(name = "ProductCode")
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }

    @XmlElement(name = "AccountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @XmlElement(name = "iBAN")
    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    @XmlElement(name = "FinancialInstitute")
    public void setFinancialInstitute(FinancialInstitute financialInstitute) {
        this.financialInstitute = financialInstitute;
    }

    public FinancialInstitute getFinancialInstitute() {
        return financialInstitute;
    }

    @XmlElement(name = "AccountOwner")
    public void setAccountOwner(String accountOwner) {
        this.accountOwner = accountOwner;
    }

    public String getAccountOwner() {
        return accountOwner;
    }
}
