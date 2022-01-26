package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DkDemoFlows {

    public static final DkDemoFlow NORDEA_LIKE =
            DkDemoFlow.builder()
                    .shouldAskNemIdUsername(true)
                    .shouldAskNemIdPassword(true)
                    .shouldAskMitIdUsername(true)
                    .shouldAskMitIdCpr(true)
                    .build();

    public static final DkDemoFlow DANSKE_LIKE =
            DkDemoFlow.builder()
                    .shouldAskNemIdUsername(false)
                    .shouldAskNemIdPassword(true)
                    .shouldAskMitIdUsername(true)
                    .shouldAskMitIdCpr(false)
                    .build();

    public static final DkDemoFlow BEC_LIKE =
            DkDemoFlow.builder()
                    .shouldAskNemIdUsername(false)
                    .shouldAskNemIdPassword(true)
                    .shouldAskMitIdUsername(false)
                    .shouldAskMitIdCpr(false)
                    .build();

    public static DkDemoFlow getFlowByName(String flowName) {
        switch (flowName) {
            case "nordea":
                return NORDEA_LIKE;
            case "danske":
                return DANSKE_LIKE;
            case "bec":
                return BEC_LIKE;
            default:
                throw new IllegalStateException("Unexpected demo flow name: " + flowName);
        }
    }
}
