package se.tink.backend.product.execution.unit.agents.seb.mortgage.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.ApiRequest;

public class GetLoanStatusRequest implements ApiRequest {
    private final String applicationId;

    public GetLoanStatusRequest(String applicationId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationId), "Missing required applicationId");

        this.applicationId = applicationId;
    }

    @Override
    public String getUriPath() {
        return String.format(
                "/loans/%s/status",
                UrlEscapers.urlPathSegmentEscaper().escape(applicationId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetLoanStatusRequest that = (GetLoanStatusRequest) o;

        return Objects.equal(this.applicationId, that.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(applicationId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("applicationId", applicationId)
                .toString();
    }
}
