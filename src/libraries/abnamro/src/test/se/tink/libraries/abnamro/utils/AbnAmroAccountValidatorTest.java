package se.tink.libraries.abnamro.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.abnamro.client.model.AmountEntity;
import se.tink.libraries.abnamro.client.model.ContractEntity;
import se.tink.libraries.abnamro.client.model.ProductEntity;

public class AbnAmroAccountValidatorTest {

    @Test
    public void testNullInput() {
        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(null);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testMissingIBAN() {
        ContractEntity contract = new ContractEntity();

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testMissingBalance() {
        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testWrongCurrency() {
        AmountEntity amountEntity = new AmountEntity();
        amountEntity.setAmount(1000D);
        amountEntity.setCurrencyCode("SEK");

        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");
        contract.setBalance(amountEntity);

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testNoProductType() {
        AmountEntity amountEntity = new AmountEntity();
        amountEntity.setAmount(1000D);
        amountEntity.setCurrencyCode("EUR");

        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");
        contract.setBalance(amountEntity);

        contract.setProduct(null);

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testInvalidProductType() {
        AmountEntity amountEntity = new AmountEntity();
        amountEntity.setAmount(1000D);
        amountEntity.setCurrencyCode("EUR");

        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");
        contract.setBalance(amountEntity);

        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductGroup("DUMMY_PRODUCT_TYPE");

        contract.setProduct(productEntity);

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertFalse(result.isValid());
    }

    @Test
    public void testValidAccount() {
        AmountEntity amountEntity = new AmountEntity();
        amountEntity.setAmount(1000D);
        amountEntity.setCurrencyCode("EUR");

        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");
        contract.setBalance(amountEntity);

        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductGroup("SAVINGS_ACCOUNTS");

        contract.setProduct(productEntity);

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertTrue(result.isValid());
    }

    @Test
    public void testValidCreditCard() {

        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");
        contract.setBalance(null); // balance entity is not needed

        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductGroup("CREDIT_CARDS_PRIVATE_AND_RETAIL");

        contract.setProduct(productEntity);

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertTrue(result.isValid());
    }

    @Test
    public void testExpiredCreditCards() {

        ContractEntity contract = new ContractEntity();
        contract.setAccountNumber("123456");
        contract.setBalance(null); // balance entity is not needed

        // Card has expired
        contract.setStatus("EXPIRED");

        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductGroup("CREDIT_CARDS_PRIVATE_AND_RETAIL");

        contract.setProduct(productEntity);

        AbnAmroAccountValidator.ValidationResult result = AbnAmroAccountValidator.validate(contract);

        Assert.assertFalse(result.isValid());
    }
}
