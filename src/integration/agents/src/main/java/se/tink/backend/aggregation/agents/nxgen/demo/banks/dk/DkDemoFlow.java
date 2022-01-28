package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DkDemoFlow {

    private boolean shouldAskNemIdUsername;
    private boolean shouldAskNemIdPassword;

    private boolean shouldAskMitIdUsername;
    private boolean shouldAskMitIdCpr;
}
