package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_HBCI_Rel_20181129_final_version.pdf
 * Page 44
 */
@Builder
public class HNSHAv2 extends BaseRequestPart {

    @NonNull private Integer securityReference;
    private String password;
    private String tanAnswer;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(securityReference);
        addGroup();
        addGroup().element(password).element(tanAnswer);
    }
}
