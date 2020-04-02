package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.SYNC_MODE_NEW_CUSTOMER_ID;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 76
 */
public class GetSynchronizationV3 extends BaseRequestPart {

    @Override
    public String getSegmentName() {
        return SegmentType.HKSYN.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 3;
    }

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(SYNC_MODE_NEW_CUSTOMER_ID);
    }
}
