package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_2018-02-23_final_version.pdf
 * Page 68
 */
public class HKTABv4 extends BaseRequestPart {
    private static final String ALL_MEDIA_TYPES = "0";
    private static final String ALL_MEDIA_CLASSES = "A";

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(ALL_MEDIA_TYPES);
        addGroup().element(ALL_MEDIA_CLASSES);
    }
}
