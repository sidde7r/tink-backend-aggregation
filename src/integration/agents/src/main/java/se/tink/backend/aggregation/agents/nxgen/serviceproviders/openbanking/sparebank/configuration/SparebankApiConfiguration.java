package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SparebankApiConfiguration {

    private String baseUrl;
    private String redirectUrl;
    private String qsealcBase64;
    private String certificateSerialNumberInHex;
    private String certificateIssuerDN;
    private String userIp;
    private boolean isUserPresent;
}
