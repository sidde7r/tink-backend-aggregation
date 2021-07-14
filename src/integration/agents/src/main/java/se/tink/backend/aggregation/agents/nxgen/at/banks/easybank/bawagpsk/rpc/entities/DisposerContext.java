package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
