package se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas;

import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;

public interface QSealcSignerProvider {
    QsealcSigner getQsealcSigner();
}
