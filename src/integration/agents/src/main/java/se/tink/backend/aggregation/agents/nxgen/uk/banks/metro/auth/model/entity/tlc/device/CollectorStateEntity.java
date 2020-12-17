package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class CollectorStateEntity {
    private static final String DISABLED = "disabled";
    private static final String ACTIVE = "active";

    private final String accounts;
    private final String devicedetails;
    private final String contacts;
    private final String owner;
    private final String software;
    private final String location;
    private final String locationcountry;
    private final String bluetooth;
    private final String externalsdkdetails;
    private final String hwauthenticators;
    private final String capabilities;
    private final String fidoauthenticators;
    private final String largedata;
    private final String localenrollments;

    public static CollectorStateEntity getDefault() {
        return CollectorStateEntity.builder()
                .accounts(DISABLED)
                .devicedetails(ACTIVE)
                .software(ACTIVE)
                .location(ACTIVE)
                .externalsdkdetails(ACTIVE)
                .hwauthenticators(ACTIVE)
                .capabilities(ACTIVE)
                .localenrollments(ACTIVE)
                .contacts(DISABLED)
                .owner(DISABLED)
                .locationcountry(DISABLED)
                .bluetooth(DISABLED)
                .fidoauthenticators(DISABLED)
                .largedata(DISABLED)
                .build();
    }
}
