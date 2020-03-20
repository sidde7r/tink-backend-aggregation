package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
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
            String profileType, String customerId, String eInvoiceStatus) {
        ProfileEntity profileEntity = new ProfileEntity(profileType, customerId);
        IncludesEntity includesEntity =
                new IncludesEntity(null, Collections.singletonList(eInvoiceStatus), null);
        FilterEntity filterEntity = new FilterEntity(Collections.singletonList(includesEntity));
        PagerEntity pagerEntity = new PagerEntity(0, 20);
        return new ResponseControlEntity(profileEntity, filterEntity, pagerEntity);
    }
}
