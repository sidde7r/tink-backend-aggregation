package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingPs256WithoutBase64SignatureCreator
        extends UkOpenBankingPs256SignatureCreator {

    public UkOpenBankingPs256WithoutBase64SignatureCreator(JwtSigner signer) {
        super(signer);
    }

    @Override
    protected Map<String, Object> createJwtHeaders() {
        final String orgId = getSoftwareStatement().getOrgId();
        final String softwareId = getSoftwareStatement().getSoftwareId();
        Objects.requireNonNull(orgId);
        Objects.requireNonNull(softwareId);

        return JwtHeaders.create().addIat().addIssWithOrgId(orgId, softwareId).addTan().build();
    }
}
