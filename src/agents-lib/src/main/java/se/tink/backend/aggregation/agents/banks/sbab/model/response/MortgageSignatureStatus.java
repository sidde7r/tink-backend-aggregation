package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Map;

public enum MortgageSignatureStatus {
    NEW("NY"),
    STARTED("STARTAD"),
    SUCCESSFUL("LYCKAD"),
    UNSUCCESSFUL("MISSLYCKAD"),
    ABORTED("AVBRUTEN"),
    EXPIRED("FORFALLEN");

    private static final Map<String, MortgageSignatureStatus> statusByString = Maps.uniqueIndex(
            Lists.newArrayList(values()),
            mortgageSignatureStatus -> mortgageSignatureStatus.status);

    private final String status;

    MortgageSignatureStatus(String status) {
        this.status = status;
    }

    public static MortgageSignatureStatus fromStatus(String status) {
        return Preconditions.checkNotNull(statusByString.get(status), "%s is not mapped to any enum value", status);
    }
}
