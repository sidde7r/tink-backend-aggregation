package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class PaymentAccountsResponseDto {

    private List<PaymentAccountInfoDto> paymentAccounts;

    private List<PermittedAccountInfoDto> ownTransferFromAccounts;

    private List<PermittedAccountInfoDto> ownTransferToAccounts;
}
