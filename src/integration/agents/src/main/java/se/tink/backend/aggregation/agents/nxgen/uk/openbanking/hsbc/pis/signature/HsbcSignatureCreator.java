package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.signature;

import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.JwtHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256SignatureCreator;

public class HsbcSignatureCreator extends UkOpenBankingPs256SignatureCreator {

    public HsbcSignatureCreator(JwtSigner signer) {
        super(signer);
    }

    @Override
    protected Map<String, Object> createJwtHeaders() {
        final String trustAnchorDomain = getTrustAnchorDomain();
        Objects.requireNonNull(trustAnchorDomain);

        return JwtHeaders.create().addIat().addIssWithRfcDn().addTan(trustAnchorDomain).build();
    }
}
