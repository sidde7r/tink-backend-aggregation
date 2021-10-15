package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentSourceAccount {

    private BigDecimal amount;
    private String bankAccountDisplayName;
    private String bankAccountDisplayNumber;
    private String bankAccountHolderFirstname;
    private String bankAccountHolderSurname;
    private String bankAccountName;
    private String bankAccountNumber;
    private int bankAccountType;
    private String bankAccountTypeDisplayName;
    private BigDecimal disposableAmount;
    private String encryptedBankAccountNumber;
    private int position;
    private int transferableAccountType;
    private String transferableAccountTypeName;
}
