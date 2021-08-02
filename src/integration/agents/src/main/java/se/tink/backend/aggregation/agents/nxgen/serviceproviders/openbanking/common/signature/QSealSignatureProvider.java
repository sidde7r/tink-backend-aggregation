package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature;

public interface QSealSignatureProvider {

    String provideSignature(QSealSignatureProviderInput input);
}
