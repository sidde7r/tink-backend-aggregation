package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.BnpPfConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transaction {
    private String accountId;
    private String transactionId;
    private String transactionReference;
    private Amount amount;
    private String creditDebitIndicator;
    private String status;
    private String bookingDateTime;
    private String valueDateTime;
    private String transactionInformation;
    private ServicerInfo servicer;
    private TransactionContextDetail transactionContextDetail;
    private BankTransactionCode bankTransactionCode;
    private MerchantDetails merchantDetails;

    public Optional<String> atmpos() {
        return Optional.ofNullable(transactionContextDetail).map(TransactionContextDetail::getAtmLocation);
    }

    public Optional<String> oremit() {
        return Optional.ofNullable(transactionInformation);
    }

    public Optional<String> ocnm() {
        return Optional.ofNullable(merchantDetails).map(MerchantDetails::getMerchantName);
    }

    public Optional<String> btc() {
        return Optional.ofNullable(transactionReference);
    }

    public se.tink.backend.aggregation.nxgen.core.transaction.Transaction toTinkTransaction(String externalAccountId) {
        return se.tink.backend.aggregation.nxgen.core.transaction.Transaction.builder()
                .setAmount(getAmount())
                .setDescription(getDescription())
                .setRawDetails(getPayloadTransactionDetails())
                .setDate(parseTransactionDate())
                .setPending(BnpPfConstants.PENDING.equalsIgnoreCase(status))
                .setExternalId(getExternalId(externalAccountId))
                .build();
    }

    private String getExternalId(String externalAccountId) {
        return externalAccountId + getTransactionTimestampString();
    }

    private se.tink.libraries.amount.Amount getAmount() {
        return amount.toTinkAmount();
    }

    private String getTransactionTimestampString() {
        return transactionId.substring(transactionId.length()-27);
    }

    private Date parseTransactionDate() {
        try {
            String transactionTimestamp = getTransactionTimestampString();
            transactionTimestamp = transactionTimestamp.substring(0, transactionTimestamp.length()-4);
            return DateUtils.parseDate(transactionTimestamp, "yyyy-MM-dd'T'HH:mm:ss.SSS");
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean matches(String domain, String code, String subCode) {
        return bankTransactionCode.getDomain().equalsIgnoreCase(domain)
                && bankTransactionCode.getCode().equalsIgnoreCase(code)
                && bankTransactionCode.getSubCode().equalsIgnoreCase(subCode);
    }

    private String getDescription() {
        for (BnpPfConstants.DescriptionRule rule : BnpPfConstants.DescriptionRule.values()) {
            if (rule.matches(this)) {
                return rule.apply(this).orElse(getDefaultDescription());
            }
        }
        return atmpos().orElse(ocnm().orElse(getDefaultDescription()));
    }

    private String getDefaultDescription() {
        return Strings.nullToEmpty(transactionReference);
    }

    private String getPayloadTransactionDetails() {
        for (BnpPfConstants.PayloadRule rule : BnpPfConstants.PayloadRule.values()) {
            if (rule.matches(this)) {
                return rule.apply(this).orElse(getDefaultDescription());
            }
        }
        return atmpos().orElse(ocnm().orElse(getDefaultDescription()));
    }
}
