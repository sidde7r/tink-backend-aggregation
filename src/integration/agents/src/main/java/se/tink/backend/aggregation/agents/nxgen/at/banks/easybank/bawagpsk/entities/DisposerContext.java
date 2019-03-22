package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DisposerContext")
public class DisposerContext {
    private String DisposerNumber;
    private FinancialInstitute FinancialInstituteObject;

    public String getDisposerNumber() {
        return DisposerNumber;
    }

    public FinancialInstitute getFinancialInstitute() {
        return FinancialInstituteObject;
    }

    @XmlElement(name = "DisposerNumber")
    public void setDisposerNumber(String DisposerNumber) {
        this.DisposerNumber = DisposerNumber;
    }

    @XmlElement(name = "FinancialInstitute")
    public void setFinancialInstitute(FinancialInstitute FinancialInstituteObject) {
        this.FinancialInstituteObject = FinancialInstituteObject;
    }
}
