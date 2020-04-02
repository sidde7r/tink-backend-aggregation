package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.Builder;
import lombok.NonNull;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 24
 */
@Builder
public class FooterV1 extends BaseRequestPart {

    @NonNull private Integer messageNumber;

    @Override
    public String getSegmentName() {
        return SegmentType.HNHBS.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 1;
    }

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(messageNumber);
    }
}
