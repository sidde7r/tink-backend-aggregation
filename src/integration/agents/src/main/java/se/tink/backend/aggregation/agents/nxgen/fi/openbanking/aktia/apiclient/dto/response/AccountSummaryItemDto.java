package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountSummaryItemDto {

    private String id;

    private String name;

    private String primaryOwnerName;

    private String iban;

    private String bic;

    private AccountTypeDto accountType;

    private BigDecimal balance;

    private BigDecimal balanceTotal;

    private BigDecimal duePaymentsTotal;

    private boolean hideFromSummary;

    private int sortingOrder;

    private FabInfoDto fabInfo;

    private List<AccountPartyDto> parties;
}
