package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.entities;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountEntity {
    private String bic;
    private String iban;
    private String accountNumber;
    private double balanceAmount;
    private double balanceAvailableAmount;
    private double balanceUsableAmount;
    private String bankName;
    private String bban;
    private String bbanType;
    private boolean canCustomize;
    private boolean canDelete;
    private boolean canShare;
    private boolean canTopUp;
    private boolean canTransferFrom;
    private boolean canTransferInstant;
    private boolean canTransferTo;
    private boolean canTransferWithFutureDate;
    private List<CapabilityEntity> capabilities;
    private long created;
    private String currency;
    private boolean deleted;
    private String displayAccountNumber;
    private String displayAccountNumberFormat;
    private String displayName;
    private String easyAccountStatus;
    private String id;
    private boolean isPrimaryAccount;
    private boolean isShared;
    private String origin;
    private String originGroupID;
    private long sort;
    private String state;
    private long updated;
}
