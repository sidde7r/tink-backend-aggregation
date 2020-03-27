package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.COUNTRY_CODE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.ENCRYPTION_BOUNDARY;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.ENCRYPTION_SUPPLIER_ROLE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.SECURITY_PROCEDURE;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_HBCI_Rel_20181129_final_version.pdf
 * Page 41
 */
@Builder
public class HNSHKv4 extends BaseRequestPart {

    @NonNull private Integer securityProcedureVersion;
    @NonNull private String securityFunction;
    @NonNull private Integer securityReference;
    @NonNull private String systemId;
    @Builder.Default private LocalDateTime creationTime = LocalDateTime.now();
    @NonNull private String blz;
    @NonNull private String username;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(SECURITY_PROCEDURE).element(securityProcedureVersion);
        addGroup().element(securityFunction);
        addGroup().element(securityReference);
        addGroup().element(ENCRYPTION_BOUNDARY);
        addGroup().element(ENCRYPTION_SUPPLIER_ROLE);
        addGroup().element(1).element().element(systemId);
        addGroup().element(1);
        addGroup()
                .element(1)
                .element(creationTime.toLocalDate())
                .element(creationTime.toLocalTime());
        addGroup().element(1).element(999).element(1); // Negotiate hash algorithm
        addGroup().element(6).element(10).element(16); // RSA mode
        addGroup()
                .element(COUNTRY_CODE)
                .element(blz)
                .element(username)
                .element("S")
                .element("0")
                .element("0");
    }
}
