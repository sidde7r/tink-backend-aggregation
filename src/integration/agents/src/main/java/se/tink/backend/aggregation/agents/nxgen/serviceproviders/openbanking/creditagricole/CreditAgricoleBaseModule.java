package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.module.QSealSignatureProviderModuleRsaSha256;

public class CreditAgricoleBaseModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new QSealSignatureProviderModuleRsaSha256());
    }
}
