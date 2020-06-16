package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.CustomerType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class UserAccountInfoDto {

    private String customerName;

    private boolean passwordUpdateRequired;

    private boolean accountLocked;

    private boolean updateOrCreateKYCInfo;

    private CustomerType customerType;

    private CustomerServiceInfoDto customerServiceInfo;

    private PersonalAdvisorDto personalAdvisor;
}
