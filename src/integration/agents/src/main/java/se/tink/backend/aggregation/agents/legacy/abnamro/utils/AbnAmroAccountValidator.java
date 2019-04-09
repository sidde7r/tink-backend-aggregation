package se.tink.backend.aggregation.agents.abnamro.utils;

import com.google.common.base.Strings;
import java.util.Set;
import se.tink.backend.aggregation.agents.abnamro.client.model.AmountEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.ContractEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.ProductEntity;

public class AbnAmroAccountValidator {

    private static final Set<String> PRODUCT_GROUPS =
            AbnAmroUtils.ACCOUNT_TYPE_BY_PRODUCT_GROUP.keySet();
    private static final Set<String> CREDIT_CARD_GROUPS = AbnAmroUtils.CREDIT_CARD_PRODUCT_GROUPS;

    /** Check whenever a contract can be converted to a Tink account */
    public static ValidationResult validate(ContractEntity contractEntity) {
        if (contractEntity == null) {
            return ValidationResult.invalid("Account is null.");
        }

        ProductEntity productEntity = contractEntity.getProduct();
        if (productEntity == null) {
            return ValidationResult.invalid("Account does not have a product type.");
        }

        // Only include contracts of specified product groups.
        if (!PRODUCT_GROUPS.contains(productEntity.getProductGroup())) {
            return ValidationResult.invalid("Product type is not valid.");
        }

        if (CREDIT_CARD_GROUPS.contains(productEntity.getProductGroup())) {

            // We only allow card that hasn't expired
            if (contractEntity.isExpired()) {
                return ValidationResult.invalid("Credit card account has expired");
            }

            return ValidationResult.valid();
        } else {
            return validateNonCreditCardContract(contractEntity);
        }
    }

    private static ValidationResult validateNonCreditCardContract(ContractEntity contractEntity) {
        // Only include accounts that have an IBAN.
        if (Strings.isNullOrEmpty(contractEntity.getAccountNumber())) {
            return ValidationResult.invalid("Account is missing IBAN.");
        }

        // Only include accounts that carry a balance.
        AmountEntity amountEntity = contractEntity.getBalance();
        if (amountEntity == null) {
            return ValidationResult.invalid("Account does not have a balance.");
        }

        // Only include accounts denoted in euro.
        if (!"EUR".equalsIgnoreCase(amountEntity.getCurrencyCode())) {
            return ValidationResult.invalid("Account is not in EUR.");
        }

        return ValidationResult.valid();
    }

    public static class ValidationResult {

        public static ValidationResult valid() {
            return new ValidationResult(true, "");
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        private ValidationResult(boolean isValid, String reason) {
            this.isValid = isValid;
            this.reason = reason;
        }

        private boolean isValid;
        private String reason;

        public boolean isValid() {
            return isValid;
        }

        public String getReason() {
            return reason;
        }
    }
}
