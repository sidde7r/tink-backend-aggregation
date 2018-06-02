package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FrontPageHighlightEntity {
    private boolean showHighlight;

    public boolean isShowHighlight() {
        return showHighlight;
    }
}
