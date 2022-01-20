package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.hsbc.pis.signature;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.JwtHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256SignatureCreator;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;

@Slf4j
public class HsbcSignatureCreator extends UkOpenBankingPs256SignatureCreator {
    private static final String TINK_LICENSE_FALLBACK_STRING =
            "CN=00158000016i44IAAQ, OID.2.5.4.97=PSDSE-FINA-44059, O=Tink AB, C=GB";
    private final String iss;

    public HsbcSignatureCreator(final UkOpenBankingFlowFacade flowFacade) {
        super(flowFacade.getJwtSinger());
        String result = null;
        try {
            Optional<X509Certificate> certificate =
                    CertificateUtils.getRootX509CertificateFromBase64EncodedString(
                            flowFacade.getAgentConfiguration().getQsealc());
            result =
                    certificate
                            .map(cert -> cert.getSubjectDN().getName())
                            .orElse(TINK_LICENSE_FALLBACK_STRING);

        } catch (CertificateException e) {
            log.error("Corrupted Qsealc and failed to generate certificate", e);
            result = TINK_LICENSE_FALLBACK_STRING;
        } finally {
            iss = result;
        }
    }

    @Override
    protected Map<String, Object> createJwtHeaders() {
        final String trustAnchorDomain = getTrustAnchorDomain();
        Objects.requireNonNull(trustAnchorDomain);
        return JwtHeaders.create().addIat().addIss(iss).addTan(trustAnchorDomain).build();
    }
}
