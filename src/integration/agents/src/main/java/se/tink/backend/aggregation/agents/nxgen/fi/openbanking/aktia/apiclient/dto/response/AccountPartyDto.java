package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.CustomerTypeCode;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.RoleCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountPartyDto {

    private CustomerTypeCode customerTypeCode;

    private String name;

    private RoleCode roleCode;
}
