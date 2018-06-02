package se.tink.backend.system.rpc;

public class CheckpointRollbackRequest {

    private String checkpointId;
    private String userId;

    public String getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(String checkpointId) {
        this.checkpointId = checkpointId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
