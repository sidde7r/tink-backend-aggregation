package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountResourceDto {

    private AccountIdentificationDto accountId;

    private List<BalanceResourceDto> balances;

    private String bicFi;

    private CashAccountType cashAccountType;

    private String details;

    private String linkedAccount;

    private String name;

    private String product;

    private String psuStatus;

    private String resourceId;

    private AccountUsage usage;

    public String creditCardIdentifier() {
        String identification = accountId.getOther().getIdentification();
        return linkedAccount + identification.substring(identification.length() - 4);
    }
}
