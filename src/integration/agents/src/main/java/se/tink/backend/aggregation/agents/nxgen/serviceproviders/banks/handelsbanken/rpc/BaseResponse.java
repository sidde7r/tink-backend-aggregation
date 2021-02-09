package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Mandate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.ErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public abstract class BaseResponse {

    @JsonProperty("_links")
    private Map<String, Link> links;

    @JsonProperty("links")
    private List<Link> linksList;

    @JsonProperty("mandates")
    private List<Mandate> mandates;

    private String code;
    private String message;
    private List<ErrorResponse> errors;
    private String detail;
    private String result;
    private String desc;

    private String autoStartToken;
    private int initialSleepTime;

    private int status;

    @JsonProperty("_links")
    public Map<String, Link> getLinks() {
        return (links != null ? links : getLinksListAsMap());
    }

    @JsonProperty("_links")
    public void setLinks(Map<String, Link> links) {
        this.links = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.links.putAll(links);
    }

    private Map<String, Link> getLinksListAsMap() {
        if (linksList == null) {
            return Collections.emptyMap();
        }
        Map<String, Link> linkMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        linkMap.putAll(
                linksList.stream().collect(Collectors.toMap(Link::getRel, Function.identity())));
        return linkMap;
    }

    protected URL findLink(Linkable linkable) {
        return searchLink(linkable)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        getLinks() + " does not contain expected url"));
    }

    protected Optional<URL> searchLink(Linkable linkable) {
        Optional<Link> optional = Optional.ofNullable(getLinks().get(linkable.getName()));
        return (optional.isPresent() ? optional.map(Link::toURL) : Optional.empty());
    }

    public boolean isCreditCard() {
        return searchLink(HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS).isPresent();
    }

    public Optional<String> getFirstErrorMessage() {
        return getErrors().stream()
                .filter(error -> error != null && !Strings.isNullOrEmpty(error.getDetail()))
                .findFirst()
                .map(input -> Optional.of(input.getDetail().trim()))
                .orElse(
                        !Strings.isNullOrEmpty(detail)
                                ? Optional.ofNullable(detail)
                                : Optional.empty());
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

    @JsonIgnore
    public List<Mandate> getMandates() {
        return mandates == null ? new ArrayList<>() : mandates;
    }

    public void setMandates(List<Mandate> mandates) {
        this.mandates = mandates;
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

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public int getResponseStatus() {
        return status;
    }
}
