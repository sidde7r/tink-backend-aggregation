package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.HandelsbankenAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.OcrCheck;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonObject
public class PaymentRecipient {

    private String name;
    private String reference;
    private String id;
    private int type;

    public OcrCheck getOcrCheck() {
        return ocrCheck;
    }

    private OcrCheck ocrCheck;

    public String getReference() {
        return reference;
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public GeneralAccountEntity retrieveGeneralAccountEntities() {
        return new HandelsbankenAccountEntity(accountIdentifier(), null, name);
    }

    public AccountIdentifier accountIdentifier() {
        Optional<String> reference = Optional.ofNullable(this.reference);
        return reference
                .filter(HandelsbankenSEConstants.Transfers.PATTERN_BG_RECIPIENT)
                .map(referenceIgnored -> bankGiroIdentifier())
                .orElseGet(
                        () ->
                                reference
                                        .filter(
                                                HandelsbankenSEConstants.Transfers
                                                        .PATTERN_PG_RECIPIENT)
                                        .map(referenceIgnored -> plusGiroIdentifier())
                                        .orElseGet(() -> nonValidIdentifier()));
    }

    private AccountIdentifier bankGiroIdentifier() {
        BankGiroIdentifier bankGiroIdentifier = new BankGiroIdentifier(this.id);
        bankGiroIdentifier.setName(name);
        return bankGiroIdentifier;
    }

    private AccountIdentifier plusGiroIdentifier() {
        PlusGiroIdentifier plusGiroIdentifier = new PlusGiroIdentifier(this.id);
        plusGiroIdentifier.setName(name);
        return plusGiroIdentifier;
    }

    private AccountIdentifier nonValidIdentifier() {
        return new NonValidIdentifier(this.id);
    }
}
