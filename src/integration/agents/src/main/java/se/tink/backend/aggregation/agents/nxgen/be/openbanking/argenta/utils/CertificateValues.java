package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CertificateValues {
    private String serialNumber;
    private String certificateAuthority;
    private String clientSigningCertificate;
}
