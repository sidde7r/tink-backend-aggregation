package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_2018-02-23_final_version.pdf
 * Page 68
 */
public class GetTanMediaInformationV4 extends BaseRequestPart {
    private static final String ALL_MEDIA_TYPES = "0";
    private static final String ALL_MEDIA_CLASSES = "A";

    @Override
    public String getSegmentName() {
        return SegmentType.HKTAB.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 4;
    }

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(ALL_MEDIA_TYPES);
        addGroup().element(ALL_MEDIA_CLASSES);
    }
}
