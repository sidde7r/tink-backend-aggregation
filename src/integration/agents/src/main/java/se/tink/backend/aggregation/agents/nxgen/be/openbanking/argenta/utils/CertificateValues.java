package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CertificateValues {
    private final String serialNumber;
    private final String certificateAuthority;
    private final String clientSigningCertificate;
}
