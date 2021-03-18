package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CreditorAccountEntity {

    private String iban;
    private String bgnr;
    private String pgnr;
    private String bban;

    @JsonIgnore
    public Creditor toTinkCreditor(PaymentProduct paymentProduct) {
        switch (paymentProduct) {
            case SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS:
                return new Creditor(AccountIdentifier.create(AccountIdentifierType.SE_PG, pgnr));
            case SWEDISH_DOMESTIC_PRIVATE_BANKGIROS:
                return new Creditor(AccountIdentifier.create(AccountIdentifierType.SE_BG, bgnr));
            default:
                if (paymentProduct == PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS
                        && bban != null) {
                    return new Creditor(new SwedishIdentifier(bban));
                } else {
                    return new Creditor(new IbanIdentifier(iban));
                }
        }
    }

    @JsonIgnore
    public static CreditorAccountEntity create(String accountNumber, String paymentProduct)
            throws CreditorValidationException {
        if (!SebUtils.isValidAccountForProduct(paymentProduct, accountNumber)) {
            throw CreditorValidationException.invalidIbanFormat("", new IllegalArgumentException());
        }

        switch (PaymentProduct.fromString(paymentProduct)) {
            case SWEDISH_DOMESTIC_PRIVATE_BANKGIROS:
                return CreditorAccountEntity.builder().bgnr(accountNumber).build();
            case SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS:
                return CreditorAccountEntity.builder().pgnr(accountNumber).build();
            case SEPA_CREDIT_TRANSFER:
                return CreditorAccountEntity.builder().iban(accountNumber).build();
            case SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS:
                if (!accountNumber.startsWith(SebConstants.MARKET)) {
                    return CreditorAccountEntity.builder().bban(accountNumber).build();
                } else {
                    return CreditorAccountEntity.builder().iban(accountNumber).build();
                }
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_PAYMENT_PRODUCT);
        }
    }
}
