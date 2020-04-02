package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.COMPRESSION_NONE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.COUNTRY_CODE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.ENCRYPTION_FUNCTION_CODE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.ENCRYPTION_SUPPLIER_ROLE;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.Constants.SECURITY_PROCEDURE;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NonNull;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_HBCI_Rel_20181129_final_version.pdf
 * Page 45
 */
@Builder
public class EncryptionHeaderV3 extends BaseRequestPart {

    private static final byte[] SEQUENCE_OF_ZEROES = "00000000".getBytes();

    @NonNull private Integer securityProcedureVersion;
    @NonNull private String systemId;
    @Builder.Default private LocalDateTime creationTime = LocalDateTime.now();
    @NonNull private String blz;
    @NonNull private String username;

    @Override
    public String getSegmentName() {
        return SegmentType.HNVSK.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 3;
    }

    @Override
    public SegmentPositionCounter assignSegmentPosition(SegmentPositionCounter counter) {
        segmentPosition = 998;
        return counter;
    }

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(SECURITY_PROCEDURE).element(securityProcedureVersion);
        addGroup().element(ENCRYPTION_FUNCTION_CODE);
        addGroup().element(ENCRYPTION_SUPPLIER_ROLE);
        addGroup().element(1).element().element(systemId);
        addGroup()
                .element(1)
                .element(creationTime.toLocalDate())
                .element(creationTime.toLocalTime());
        addGroup()
                .element(2)
                .element(2)
                .element(13)
                .element(SEQUENCE_OF_ZEROES)
                .element(5)
                .element(1); // Crypto algorithm
        addGroup()
                .element(COUNTRY_CODE)
                .element(blz)
                .element(username)
                .element("S")
                .element(0)
                .element(0);
        addGroup().element(COMPRESSION_NONE);
    }
}
