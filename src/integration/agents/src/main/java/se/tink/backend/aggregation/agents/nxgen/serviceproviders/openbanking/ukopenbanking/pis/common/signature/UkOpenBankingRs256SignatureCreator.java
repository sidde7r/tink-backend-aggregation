package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

@RequiredArgsConstructor
public class UkOpenBankingRs256SignatureCreator implements UkOpenBankingSignatureCreator {

    private final JwtSigner signer;

    @Setter private String softwareId;

    @Override
    public String createSignature(Map<String, Object> payloadClaims) {
        Objects.requireNonNull(softwareId);
        return signer.sign(
                JwtSigner.Algorithm.RS256, createJwtHeaders(softwareId), payloadClaims, true);
    }

    @VisibleForTesting
    Map<String, Object> createJwtHeaders(String softwareId) {
        return JwtHeaders.create()
                .addB64()
                .addIatWithMillis(new Date().getTime() - 1000)
                .addIss(softwareId)
                .build();
    }
}
