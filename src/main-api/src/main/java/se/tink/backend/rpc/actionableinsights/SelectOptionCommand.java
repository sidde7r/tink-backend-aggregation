package se.tink.backend.rpc.actionableinsights;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class SelectOptionCommand {
    private String userId;
    private String insightsId;
    private String optionId;

    public SelectOptionCommand(String userId, String insightsId, String optionId) {
        validate(userId, insightsId, optionId);
        this.userId = userId;
        this.insightsId = insightsId;
        this.optionId = optionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getInsightsId() {
        return insightsId;
    }

    public String getOptionId() {
        return optionId;
    }

    private void validate(String userId, String insightsId, String optionId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(insightsId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(optionId));
    }
}
