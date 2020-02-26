package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseControlEntity {
    private ProfileEntity profile;
    private FilterEntity filter;
    private PagerEntity pager;

    private ResponseControlEntity(ProfileEntity profile, FilterEntity filter, PagerEntity pager) {
        this.profile = profile;
        this.filter = filter;
        this.pager = pager;
    }

    @JsonIgnore
    public static ResponseControlEntity of(
            String profileType, String customerId, String transactionStatus, int page) {
        ProfileEntity profileEntity = new ProfileEntity(profileType, customerId);
        FilterEntity filterEntity = FilterEntity.of(transactionStatus);
        PagerEntity pagerEntity = new PagerEntity(0, page);
        return new ResponseControlEntity(profileEntity, filterEntity, pagerEntity);
    }
}
