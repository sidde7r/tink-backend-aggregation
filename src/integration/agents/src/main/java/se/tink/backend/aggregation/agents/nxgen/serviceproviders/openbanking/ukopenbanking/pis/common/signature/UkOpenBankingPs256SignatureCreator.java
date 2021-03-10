package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

@RequiredArgsConstructor
public abstract class UkOpenBankingPs256SignatureCreator implements UkOpenBankingSignatureCreator {

    private final JwtSigner signer;

    @Setter
    @Getter(value = AccessLevel.PROTECTED)
    private SoftwareStatementAssertion softwareStatement;

    @Override
    public String createSignature(Map<String, Object> payloadClaims) {
        return signer.sign(JwtSigner.Algorithm.PS256, createJwtHeaders(), payloadClaims, true);
    }

    protected abstract Map<String, Object> createJwtHeaders();
}
