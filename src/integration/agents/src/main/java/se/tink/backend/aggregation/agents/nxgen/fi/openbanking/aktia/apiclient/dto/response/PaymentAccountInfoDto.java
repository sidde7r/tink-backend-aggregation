package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentAccountInfoDto extends PermittedAccountInfoDto {

    private boolean defaultPaymentAccount;
}
