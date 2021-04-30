package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FiduciaHeaderValues {

    private String tppOrganizationIdentifier;
    private String sealcDerBase64;
    private String qsealcSerialNumberInHex;
    private String qsealcIssuerDN;
    private String redirectUrl;
    private String userIp;
}
