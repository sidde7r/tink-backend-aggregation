package se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaAgentUtils;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.social.security.SocialSecurityNumber;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BeneficiaryEntity implements GeneralAccountEntity {
    private static final AggregationLogger log = new AggregationLogger(BeneficiaryEntity.class);
    private static final int NORDEA_CLEARING_LENGTH = 4;
    private static final String NORDEA_PERSONKONTO_CLEARING_NUMBER = "3300";

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String beneficiaryNickName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentSubTypeExtension;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String category;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String beneficiaryBankId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String toAccountId;

    private Map<String, Object> beneficiaryId;

    public String getBeneficiaryNickName() {
        return beneficiaryNickName;
    }

    public void setBeneficiaryNickName(String beneficiaryNickName) {
        this.beneficiaryNickName = beneficiaryNickName;
    }

    public String getPaymentSubTypeExtension() {
        return paymentSubTypeExtension;
    }

    public void setPaymentSubTypeExtension(String paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBeneficiaryBankId() {
        return beneficiaryBankId;
    }

    public void setBeneficiaryBankId(String beneficiaryBankId) {
        this.beneficiaryBankId = beneficiaryBankId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Map<String, Object> getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(Map<String, Object> beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getCleanedAccountNumber() {
        return Strings.nullToEmpty(toAccountId).replace("-", "").replace(" ", "").trim();
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        String cleanedAccountNumber = getCleanedAccountNumber();

        if (Objects.equals(this.paymentType, "ThirdParty")) {
            return toSwedishIdentifier(cleanedAccountNumber);
        } else {
            return toGiroIdentifier(cleanedAccountNumber);
        }
    }

    public AccountIdentifier toGiroIdentifier(String cleanedAccountNumber) {
        if (this.isBgPaymentEntity()) {
            return new BankGiroIdentifier(cleanedAccountNumber);
        } else if (this.isPgPaymentEntity()) {
            return new PlusGiroIdentifier(cleanedAccountNumber);
        } else {
            log.warn("Unknown payment identifier: " + paymentSubTypeExtension);
            return new NonValidIdentifier(null);
        }
    }

    private AccountIdentifier toSwedishIdentifier(String cleanedAccountNumber) {
        Optional<ClearingNumber.Bank> lookupBankResult = NordeaAgentUtils
                .lookupBankFromBeneficiaryId(beneficiaryBankId);

        if (!lookupBankResult.isPresent()) {
            log.warn(
                    String.format("Unknown beneficiary bank id "
                            + "(beneficiaryBankId: %s, cleanedAccountNumber: %s)",
                            beneficiaryBankId, cleanedAccountNumber));
            return new SwedishIdentifier((String)null);
        }

        if (isNordeaPersonkonto(cleanedAccountNumber, lookupBankResult.get())) {
            return new SwedishIdentifier(NORDEA_PERSONKONTO_CLEARING_NUMBER + cleanedAccountNumber);
        } else if (isNordeaBank(lookupBankResult.get()) && !hasValidNordeaClearing(cleanedAccountNumber)) {
            log.warn(String.format("Nordea recipient not matching clearing lookup "
                    + "(beneficiaryBankId: %s, cleanedAccountNumber: %s)",
                    beneficiaryBankId, cleanedAccountNumber));

            return new SwedishIdentifier((String)null);
        }

        return new SwedishIdentifier(cleanedAccountNumber);
    }

    private static boolean hasValidNordeaClearing(String cleanedAccountNumber) {
        if (Strings.isNullOrEmpty(cleanedAccountNumber) || cleanedAccountNumber.length() < NORDEA_CLEARING_LENGTH) {
            return false;
        }

        String potentialNordeaClearingNumber = cleanedAccountNumber.substring(0, NORDEA_CLEARING_LENGTH);
        Optional<ClearingNumber.Details> lookedUpClearingDetails = ClearingNumber.get(potentialNordeaClearingNumber);

        return lookedUpClearingDetails.isPresent() &&
                Objects.equals(lookedUpClearingDetails.get().getBank(), ClearingNumber.Bank.NORDEA);
    }

    private static boolean isNordeaPersonkonto(String cleanedAccountNumber, ClearingNumber.Bank lookupBankResult) {
        if (!isNordeaBank(lookupBankResult)) {
            return false;
        } else if (Strings.isNullOrEmpty(cleanedAccountNumber) || cleanedAccountNumber.length() != 10) {
            return false;
        }

        return new SocialSecurityNumber.Sweden(cleanedAccountNumber).isValid();
    }

    private static boolean isNordeaBank(ClearingNumber.Bank lookupBankResult) {
        return Objects.equals(lookupBankResult, ClearingNumber.Bank.NORDEA) ||
                Objects.equals(lookupBankResult, ClearingNumber.Bank.NORDEA_PERSONKONTO);
    }

    @Override
    public String generalGetBank() {
        AccountIdentifier identifier = generalGetAccountIdentifier();
        if (identifier.is(Type.SE) && identifier.isValid()) {
            SwedishIdentifier swedishIdentifier = identifier.to(SwedishIdentifier.class);
            return swedishIdentifier.getBankName();
        } else {
            return null;
        }
    }

    @Override
    public String generalGetName() {
        return getBeneficiaryNickName();
    }

    public boolean isBankTransferEntity() {
        return Objects.equals(this.paymentType, "ThirdParty");
    }

    public boolean isPaymentEntity() {
        return Objects.equals(this.paymentType, "Normal");
    }

    public boolean isPgPaymentEntity() {
        return Objects.equals(this.paymentSubTypeExtension, "PGType");
    }

    public boolean isBgPaymentEntity() {
        return Objects.equals(this.paymentSubTypeExtension, "BGType");
    }
}
