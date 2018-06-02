package se.tink.backend.rpc;

import java.util.List;

public class DeleteUserRequest {
    private String comment;
    private List<String> reasons;
    private String userId;

    public String getComment() {
        return comment;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getUserId() {
        return userId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
