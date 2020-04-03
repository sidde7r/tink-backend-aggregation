package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.SYNC_MODE_NEW_CUSTOMER_ID;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 76
 */
public class HKSYNv3 extends BaseRequestPart {

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(SYNC_MODE_NEW_CUSTOMER_ID);
    }
}
