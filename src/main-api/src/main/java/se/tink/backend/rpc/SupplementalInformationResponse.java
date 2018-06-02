package se.tink.backend.rpc;

import com.google.common.base.MoreObjects;

public class SupplementalInformationResponse {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("content", content).toString();
    }
}
