package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.COUNTRY_CODE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.SYSTEM_ID_REQUIRED;

import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf
 * Page 51
 */
@Builder
public class HKIDNv2 extends BaseRequestPart {

    @NonNull private String systemId;
    @NonNull private String blz;
    @NonNull private String username;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(COUNTRY_CODE).element(blz);
        addGroup().element(username);
        addGroup().element(systemId);
        addGroup().element(SYSTEM_ID_REQUIRED);
    }
}
