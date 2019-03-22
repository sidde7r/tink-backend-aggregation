package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "FinancialInstitute")
@XmlType(propOrder = {"bankCode", "BIC", "code", "shortName"})
public class FinancialInstitute {
    private String BankCode;
    private String ShortName;
    private String bic;
    private String code;

    @XmlElement(name = "BankCode")
    public void setBankCode(String BankCode) {
        this.BankCode = BankCode;
    }

    public String getBankCode() {
        return BankCode;
    }

    @XmlElement(name = "ShortName")
    public void setShortName(String ShortName) {
        this.ShortName = ShortName;
    }

    public String getShortName() {
        return ShortName;
    }

    @XmlElement(name = "BIC")
    public void setBIC(String bic) {
        this.bic = bic;
    }

    public String getBIC() {
        return bic;
    }

    @XmlElement(name = "Code")
    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
