package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class WlDeviceAutoProvisioningRealmEntity {
    @JsonProperty("ID")
    private IDEntity iD;
    @JsonProperty("CSR")
    private String csr;
    @JsonProperty("certificate")
    private String certificate;

    public void setID(IDEntity iD) {
        this.iD = iD;
    }

    public IDEntity getID() {
        return iD;
    }

    public void setCsr(String csr) {
        this.csr = csr;
    }

    public String getCsr() {
        return csr;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCertificate() {
        return certificate;
    }
}
