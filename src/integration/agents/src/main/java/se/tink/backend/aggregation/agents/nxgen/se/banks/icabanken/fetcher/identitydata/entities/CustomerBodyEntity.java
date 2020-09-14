package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(UpperCamelCaseStrategy.class)
@Getter
public class CustomerBodyEntity {
    private String personalIdentityNumber;
    private String firstName;
    private String lastName;
    private AddressEntity address;
    private String mobilePhone;
    private String homePhone;
    private String email;
    private boolean isStudent;
    private boolean isDeceased;
    private EngagementEntity engagement;
    private boolean isUnderAdministration;
    private String mainFrameCustomerId;
    private boolean receiveOffers;
    private boolean isCustomer;
    private boolean verifiedIdentification;
    private boolean updatedKDK;
    private boolean isFullyLoaded;
    private boolean hasCustodianApprovalToApplyForMobileBankId;
    private boolean isMBIDApproved;
    private boolean expectKDK;
}
