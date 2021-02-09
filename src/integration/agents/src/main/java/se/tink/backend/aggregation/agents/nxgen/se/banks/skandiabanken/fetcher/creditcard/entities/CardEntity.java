package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities.HolderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CardEntity {
    private HolderEntity holder;
    private int status;
    private String statusName;
    private boolean isActive;
    private boolean ownedBySelf;
    private int cardType;
    private ExpirationEntity expiration;
    private String encryptedNumber;
    private String displayTypeName;
    private int position;
    private String bankAccountReference;
    private String reference;
    private String displayName; // masked card number
    private String name;
    private String number;
    private String typeName;
}
