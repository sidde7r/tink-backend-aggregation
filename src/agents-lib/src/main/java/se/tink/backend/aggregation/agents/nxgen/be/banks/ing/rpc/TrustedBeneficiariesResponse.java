package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml.XmlRequestListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml.XmlHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.entities.BeneficiaryListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "mobileResponse")
public class TrustedBeneficiariesResponse {
    private XmlHeaderEntity header;
    private String returnCode;
    private BeneficiaryListEntity beneficiaries;
    private XmlRequestListEntity requests;

    public XmlHeaderEntity getHeader() {
        return header;
    }

    public void setHeader(XmlHeaderEntity header) {
        this.header = header;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public BeneficiaryListEntity getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(BeneficiaryListEntity beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public XmlRequestListEntity getRequests() {
        return requests;
    }

    public void setRequests(XmlRequestListEntity requests) {
        this.requests = requests;
    }
}
