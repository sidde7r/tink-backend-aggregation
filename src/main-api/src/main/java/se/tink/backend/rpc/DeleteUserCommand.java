package se.tink.backend.rpc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.api.headers.TinkHttpHeaders;

public class DeleteUserCommand {
    private List<String> reasons;
    private String comment;
    private Optional<String> remoteAddress = Optional.empty();
    private Map<String, String> headers;

    public DeleteUserCommand(List<String> reasons, String comment,
            Map<String, String> headers) {
        this.reasons = reasons;
        this.comment = comment;
        this.headers = headers;
        if (headers != null) {
            this.remoteAddress = Optional.ofNullable(headers.get(TinkHttpHeaders.FORWARDED_FOR_HEADER_NAME));
        }
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getComment() {
        return comment;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
