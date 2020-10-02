package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.LocalKeySigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

/** @deprecated Please use @{@link JwksKeySignerProvider} */
@Deprecated
public final class LocalKeySignerProvider implements Provider<JwtSigner> {

    private final UkOpenBankingConfiguration configuration;

    @Inject
    private LocalKeySignerProvider(UkOpenBankingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public JwtSigner get() {
        return new LocalKeySigner(
                configuration.getSigningKeyId(),
                RSA.getPrivateKeyFromBytes(
                        EncodingUtils.decodeBase64String(configuration.getSigningKey())));
    }
}
