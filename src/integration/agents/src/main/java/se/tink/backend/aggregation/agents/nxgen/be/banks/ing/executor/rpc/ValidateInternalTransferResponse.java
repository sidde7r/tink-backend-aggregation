package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc;

import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml.XmlErrorListEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml.XmlHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.xml.XmlRequestListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "mobileResponse")
public class ValidateInternalTransferResponse {
    private XmlHeaderEntity header;
    private String returnCode;
    private XmlErrorListEntity errors;
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

    public XmlErrorListEntity getErrors() {
        return errors;
    }

    public void setErrors(XmlErrorListEntity errors) {
        this.errors = errors;
    }

    public XmlRequestListEntity getRequests() {
        return requests;
    }

    public void setRequests(XmlRequestListEntity requests) {
        this.requests = requests;
    }
}
