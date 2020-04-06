package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.DEFAULT_BPD_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.DEFAULT_UPD_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.LANGUAGE_DE;

import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 53
 */
@Builder
public class HKVVBv3 extends BaseRequestPart {

    @NonNull private String productId;
    @NonNull private String productVersion;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(DEFAULT_BPD_VERSION);
        addGroup().element(DEFAULT_UPD_VERSION);
        addGroup().element(LANGUAGE_DE);
        addGroup().element(productId);
        addGroup().element(productVersion);
    }
}
