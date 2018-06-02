package se.tink.backend.rpc.actionableinsights;

public class GetInsightsCommand {
    private String userId;

    public GetInsightsCommand(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
