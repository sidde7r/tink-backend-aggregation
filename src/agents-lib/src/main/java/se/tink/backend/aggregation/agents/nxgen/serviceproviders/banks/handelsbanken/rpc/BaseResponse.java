package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.ErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public abstract class BaseResponse {

    //other fields:
    // _links

    private List<Link> links;
    private String code;
    private String message;
    private List<ErrorResponse> errors;
    private String detail;
    private String result;
    private String desc;

    public List<Link> getLinks() {
        if (links == null) {
            links = Collections.emptyList();
        }
        return links;
    }

    public void setLinks(
            List<Link> links) {
        this.links = links;
    }

    protected URL findLink(Linkable linkable) {
        return searchLink(linkable)
                .orElseThrow(() -> new IllegalStateException(links + " does not contain expected url"));
    }

    protected Optional<URL> searchLink(Linkable linkable) {
        return getLinks().stream()
                .filter(link -> link != null && linkable.getName().equalsIgnoreCase(link.getRel()))
                .findFirst()
                .map(Link::toURL);
    }

    public Optional<String> getFirstErrorMessage() {
        return getErrors().stream().filter(
                error -> error != null && !Strings.isNullOrEmpty(error.getDetail()))
                .findFirst()
                .map(input -> Optional.of(input.getDetail().trim()))
                .orElse(!Strings.isNullOrEmpty(detail) ? Optional.ofNullable(detail) : Optional.empty());
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public List<ErrorResponse> getErrors() {
        if (errors == null) {
            errors = Collections.emptyList();
        }
        return errors;
    }

    public String getResult() {
        return result;
    }

    public String getDesc() {
        return desc;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
