package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 62
 */
@Builder
public class HKENDv1 extends BaseRequestPart {

    @NonNull private String dialogId;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(dialogId);
    }
}
