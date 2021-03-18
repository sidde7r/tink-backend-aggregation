package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.HandelsbankenAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;

@JsonObject
public class HandelsbankenSEPaymentAccount
        implements TransferSignRequest.AmountableSource, TransferSignRequest.AmountableDestination {
    private String number;
    private String numberFormatted;
    private String name;

    public boolean hasIdentifier(String identifier) {
        return identifier != null && identifier.equals(number);
    }

    @Override
    public TransferAmount toTransferAmount() {
        return TransferAmount.from(number, numberFormatted, name);
    }

    public TransferSignRequest.AmountableDestination asAmountableDestination() {
        return this;
    }

    @Override
    public boolean isKnownDestination() {
        return true;
    }

    public GeneralAccountEntity toGeneralAccountEntity() {
        AccountIdentifier accountIdentifier = toAccountIdentifier();
        String bankName = getBankName(accountIdentifier);
        return new HandelsbankenAccountEntity(accountIdentifier, bankName, name);
    }

    private String getBankName(AccountIdentifier identifier) {
        if (identifier.isValid()) {
            if (identifier.getType() == AccountIdentifierType.SE_SHB_INTERNAL) {
                return "Handelsbanken";
            }
            return identifier.to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    private AccountIdentifier toAccountIdentifier() {
        if (isSwedish()) {
            return new SwedishIdentifier(number);
        }
        return new SwedishSHBInternalIdentifier(number);
    }

    private boolean isSwedish() {
        return numberFormatted != null && numberFormatted.contains("-");
    }
}
