package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Messages_Geschaeftsvorfaelle_2015-08-07_final_version.pdf
 * Page 375
 */
public class GetSepaDetailsV1 extends BaseRequestPart {

    @Override
    public String getSegmentName() {
        return SegmentType.HKSPA.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 1;
    }
}
