package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ResponseControlEntity {
    private ProfileEntity profile;
    private FilterEntity filter;
    private PagerEntity pager;

    @JsonIgnore
    public static ResponseControlEntity of(
            String profileType, String customerId, String transactionStatus, int page) {
        ProfileEntity profileEntity = new ProfileEntity(profileType, customerId);
        FilterEntity filterEntity = FilterEntity.of(transactionStatus);
        PagerEntity pagerEntity = new PagerEntity(0, page);
        return new ResponseControlEntity(profileEntity, filterEntity, pagerEntity);
    }
}
