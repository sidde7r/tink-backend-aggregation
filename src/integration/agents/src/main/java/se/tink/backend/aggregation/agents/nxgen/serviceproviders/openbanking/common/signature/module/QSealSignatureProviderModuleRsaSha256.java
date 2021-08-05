package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.SignatureProviderRsaSha256;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;

public final class QSealSignatureProviderModuleRsaSha256 extends AbstractModule {

    @Override
    protected void configure() {
        install(new QSealcSignerModuleRSASHA256());
    }

    @Singleton
    @Provides
    public QSealSignatureProvider provideQSealSignatureProvider(QsealcSigner qsealcSigner) {
        return new SignatureProviderRsaSha256(qsealcSigner);
    }
}
