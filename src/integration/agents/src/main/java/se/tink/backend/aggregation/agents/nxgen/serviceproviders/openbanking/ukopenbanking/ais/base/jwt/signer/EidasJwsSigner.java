package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.impl.BaseJWSProvider;
import com.nimbusds.jose.util.Base64URL;
import java.util.Set;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class EidasJwsSigner extends BaseJWSProvider implements JWSSigner {

    private static final Set<JWSAlgorithm> SUPPORTED_ALGORITHMS =
            ImmutableSet.of(JWSAlgorithm.RS256, JWSAlgorithm.PS256);

    private final InternalEidasProxyConfiguration configuration;
    private final EidasIdentity identity;

    EidasJwsSigner(InternalEidasProxyConfiguration configuration, EidasIdentity identity) {
        super(SUPPORTED_ALGORITHMS);
        this.configuration = configuration;
        this.identity = identity;
    }

    @Override
    public Base64URL sign(JWSHeader header, byte[] signingInput) {
        JWSAlgorithm alg = header.getAlgorithm();
        if (alg.equals(JWSAlgorithm.RS256)) {
            QsealcSigner signer =
                    QsealcSignerImpl.build(configuration, QsealcAlg.EIDAS_RSA_SHA256, identity);
            return Base64URL.encode(signer.getSignature(signingInput));
        } else if (alg.equals(JWSAlgorithm.PS256)) {
            QsealcSigner signer =
                    QsealcSignerImpl.build(configuration, QsealcAlg.EIDAS_PSS_SHA256, identity);
            return Base64URL.encode(signer.getSignature(signingInput));
        }

        throw new IllegalStateException(
                "Code should not reach here. Make sure that all algs listed in SUPPORTED_ALGORITHMS list are implemented.");
    }
}
