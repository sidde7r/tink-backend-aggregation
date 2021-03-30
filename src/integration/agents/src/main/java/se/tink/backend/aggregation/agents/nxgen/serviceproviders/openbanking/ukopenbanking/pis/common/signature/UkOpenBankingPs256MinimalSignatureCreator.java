package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingPs256MinimalSignatureCreator extends UkOpenBankingPs256SignatureCreator {

    public UkOpenBankingPs256MinimalSignatureCreator(JwtSigner signer) {
        super(signer);
    }

    @Override
    protected Map<String, Object> createJwtHeaders() {
        final String trustAnchorDomain = getTrustAnchorDomain();
        Objects.requireNonNull(trustAnchorDomain);

        return JwtHeaders.create().addTan(trustAnchorDomain).build();
    }
}
