package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 24
 */
@Builder
public class HNHBSv1 extends BaseRequestPart {

    @NonNull private Integer messageNumber;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(messageNumber);
    }
}
