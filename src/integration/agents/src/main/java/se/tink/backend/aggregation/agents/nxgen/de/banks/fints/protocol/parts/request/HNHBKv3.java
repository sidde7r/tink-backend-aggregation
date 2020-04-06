package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 23
 */
@Builder
public class HNHBKv3 extends BaseRequestPart {

    @Builder.Default private Integer messageLength = 0;
    @Builder.Default private String finTsVersion = "300";
    @NonNull private String dialogId;
    @NonNull private Integer messageNumber;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(formatLength(messageLength));
        addGroup().element(finTsVersion);
        addGroup().element(dialogId);
        addGroup().element(messageNumber);
    }

    private String formatLength(int length) {
        return String.format("%012d", length);
    }
}
