package se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas;

import com.google.inject.Inject;
import se.tink.agent.sdk.utils.signer.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;

public class WiremockQSealcSignerProvider implements QSealcSignerProvider {
    private final FakeQsealcSigner qsealcSigner;

    @Inject
    public WiremockQSealcSignerProvider() {
        this.qsealcSigner = new FakeQsealcSigner();
    }

    @Override
    public QsealcSigner getQsealcSigner() {
        return this.qsealcSigner;
    }
}
