package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAccountEntity {
    public static Map<PaymentProduct, Function<String, CreditorAccountEntity>>
            paymentProductsMapper = new HashMap<>();

    static {
        paymentProductsMapper.put(
                PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS,
                CreditorAccountEntity::ofPgnrRequest);

        paymentProductsMapper.put(
                PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS,
                CreditorAccountEntity::ofBgnrRequest);

        paymentProductsMapper.put(
                PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS,
                CreditorAccountEntity::ofIbanRequest);

        paymentProductsMapper.put(
                PaymentProduct.SEPA_CREDIT_TRANSFER, CreditorAccountEntity::ofIbanRequest);
    }

    private String iban;
    private String bgnr;
    private String pgnr;

    public CreditorAccountEntity() {}

    @JsonIgnore
    private CreditorAccountEntity(String iban, String pgnr, String bgnr) {
        this.iban = iban;
        this.pgnr = pgnr;
        this.bgnr = bgnr;
    }

    @JsonIgnore
    private static CreditorAccountEntity ofPgnrRequest(String accountNumber) {
        return new CreditorAccountEntity(null, accountNumber, null);
    }

    @JsonIgnore
    private static CreditorAccountEntity ofIbanRequest(String accountNumber) {
        return new CreditorAccountEntity(accountNumber, null, null);
    }

    @JsonIgnore
    private static CreditorAccountEntity ofBgnrRequest(String accountNumber) {
        return new CreditorAccountEntity(null, null, accountNumber);
    }

    @JsonIgnore
    public Creditor toTinkCreditor(PaymentProduct paymentProduct) {
        switch (paymentProduct) {
            case SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS:
                return new Creditor(AccountIdentifier.create(Type.SE_PG, pgnr));
            case SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS:
                return new Creditor(AccountIdentifier.create(Type.SE_BG, bgnr));
            default:
                return new Creditor(new IbanIdentifier(iban));
        }
    }

    @JsonIgnore
    public static CreditorAccountEntity create(String accountNumber, String paymentProduct)
            throws CreditorValidationException {
        if (Stream.of(
                        PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS.getValue(),
                        PaymentProduct.SEPA_CREDIT_TRANSFER.getValue())
                .anyMatch(paymentProduct::equalsIgnoreCase)) {
            IbanIdentifier creditorIban = new IbanIdentifier(accountNumber);
            if (!creditorIban.isValidIban()) {
                throw CreditorValidationException.invalidIbanFormat(
                        "", new IllegalArgumentException());
            }
        }

        return paymentProductsMapper
                .get(PaymentProduct.fromString(paymentProduct))
                .apply(accountNumber);
    }
}
