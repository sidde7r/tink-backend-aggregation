package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_2018-02-23_final_version.pdf
 * Page 45
 */
@Builder
public class HKTANv6 extends BaseRequestPart {

    @AllArgsConstructor
    @Getter
    public enum TanProcessVariant {
        CHALLENGE("1"),
        TAN("2"),
        TAN_INITIALIZE_MULTIPLE("3"),
        TAN_INITIALIZE_SINGLE("4");
        private String code;
    }

    private TanProcessVariant tanProcessVariant;
    private se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType
            segmentType;
    private String taskHashValue;
    private String taskReference;
    @Builder.Default private Boolean furtherTanFollows = false;
    private Boolean cancelTask;
    private Integer challengeClass;
    private String parameterChallengeClass;
    private String tanMediumName;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(tanProcessVariant.getCode());
        addGroup().element(segmentType != null ? segmentType.getSegmentName() : "");
        addGroup();
        addGroup().element(taskHashValue);
        addGroup().element(taskReference);
        addGroup().element(furtherTanFollows);
        addGroup();
        addGroup();
        addGroup();
        addGroup();
        addGroup().element(tanMediumName);
    }
}
