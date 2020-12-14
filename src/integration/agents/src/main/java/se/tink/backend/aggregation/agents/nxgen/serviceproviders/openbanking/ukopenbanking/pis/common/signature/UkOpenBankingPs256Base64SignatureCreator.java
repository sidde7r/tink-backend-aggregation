package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

public class UkOpenBankingPs256Base64SignatureCreator extends UkOpenBankingPs256SignatureCreator {

    public UkOpenBankingPs256Base64SignatureCreator(JwtSigner signer) {
        super(signer);
    }

    @Override
    protected Map<String, Object> createJwtHeaders() {
        return JwtHeaders.create().addB64().addIat().addIssWithTinkOrgId().addTan().build();
    }
}
