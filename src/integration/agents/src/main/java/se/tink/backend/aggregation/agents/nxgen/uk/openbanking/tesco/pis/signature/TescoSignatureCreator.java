package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tesco.pis.signature;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.JwtHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256SignatureCreator;

public class TescoSignatureCreator extends UkOpenBankingPs256SignatureCreator {

    public TescoSignatureCreator(JwtSigner signer) {
        super(signer);
    }

    @Override
    protected Map<String, Object> createJwtHeaders() {
        return JwtHeaders.create().addB64().addIat().addTescoSpecialIss().addTan().build();
    }
}
