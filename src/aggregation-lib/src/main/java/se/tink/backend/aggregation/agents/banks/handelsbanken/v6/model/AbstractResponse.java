package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractResponse {
    protected String code;
    protected String desc;
    @JsonProperty("_links")
    protected HashMap<String, LinkEntity> linksMap;
    protected List<LinkEntity> links;
    protected String message;
    private List<ResponseError> errors;
    private String detail;
    private String status;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public HashMap<String, LinkEntity> getLinksMap() {
        return linksMap != null ? linksMap : Maps.newHashMap();
    }

    public void setLinksMap(HashMap<String, LinkEntity> linksMap) {
        this.linksMap = linksMap;
    }

    public List<LinkEntity> getLinks() {
        return links != null ? links : Lists.newArrayList();
    }

    public void setLinks(List<LinkEntity> links) {
        this.links = links;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResponseError> getErrors() {
        return errors;
    }

    public void setErrors(List<ResponseError> errors) {
        this.errors = errors;
    }

    @JsonIgnore
    private Optional<ResponseError> getFirstErrorWithErrorText() {
        if (getErrors() != null) {
            return getErrors().stream().filter(
                    input -> input != null && input.getDetail() != null && !input.getDetail().trim().isEmpty())
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    @JsonIgnore
    public Optional<String> getFirstErrorMessage() {
        if (getFirstErrorWithErrorText().isPresent()) {
            return getFirstErrorWithErrorText().map(input -> input.getDetail().trim());
        } else if (Strings.isNullOrEmpty(detail)) {
            return Optional.ofNullable(detail);
        } else {
            return Optional.empty();
        }
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
