package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcAgreementDtoTest {

    private static final String FAKE_IBAN = "BE62456595893861"; // http://randomiban.com/?country=Belgium

    @Test
    public void shouldNotSetBusinessFlag() {
        AgreementDto agreementDto = SerializationUtils.deserializeFromString(AGREEMENT_NO_BUSINESS, AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        org.junit.Assert.assertTrue(
                "No account flags should be set", transactionalAccount.getAccountFlags().isEmpty());
    }

    @Test
    public void shouldSetBusinessFlag() {
        AgreementDto agreementDto = SerializationUtils.deserializeFromString(AGREEMENT_BUSINESS, AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        org.junit.Assert.assertTrue(
                "Business flag set",
                transactionalAccount.getAccountFlags().get(0).equals(AccountFlag.BUSINESS));
    }

    @Test
    public void shouldHandleMissingBusinessFlag() {
        AgreementDto agreementDto = SerializationUtils.deserializeFromString(
                AGREEMENT_MISSING_BUSINESS_TAG,
                AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        org.junit.Assert.assertTrue(
                "No account flags should be set", transactionalAccount.getAccountFlags().isEmpty());
    }

    @Test
    public void shouldMapCheckingAccountType() {
        String checkingAccountType = "0030";
        AgreementDto agreementDto =
                SerializationUtils.deserializeFromString(
                        AGREEMENT_MAPPINGTEST.replace("MAPPING_VALUE", checkingAccountType),
                        AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        Assert.assertEquals(AccountTypes.CHECKING, transactionalAccount.getType());
    }

    @Test
    public void shouldMapSavingsAccountType() {
        String savingsAccountType = "3591";
        AgreementDto agreementDto =
                SerializationUtils.deserializeFromString(
                        AGREEMENT_MAPPINGTEST.replace("MAPPING_VALUE", savingsAccountType),
                        AgreementDto.class);
        Assert.assertEquals(AccountTypes.SAVINGS, agreementDto.toTransactionalAccount().getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldMapUnknownAccountType() {
        String randomAccountType = "OTHER";
        AgreementDto agreementDto =
                SerializationUtils.deserializeFromString(
                        AGREEMENT_MAPPINGTEST.replace("MAPPING_VALUE", randomAccountType),
                        AgreementDto.class);
        agreementDto.toTransactionalAccount();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldMapIgnoredAccountType() {
        String checkingAccountType = "1013";
        AgreementDto agreementDto =
                SerializationUtils.deserializeFromString(
                        AGREEMENT_MAPPINGTEST.replace("MAPPING_VALUE", checkingAccountType),
                        AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();
    }

    private static final String AGREEMENT_NO_BUSINESS = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"3844\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isBusiness\":{\"V\":\"false\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"20170905233735244689\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";
    private static final String AGREEMENT_BUSINESS = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"3844\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isBusiness\":{\"V\":\"true\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"20170905233735244689\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";
    private static final String AGREEMENT_MISSING_BUSINESS_TAG = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"3844\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"20170905233735244689\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";
    private static final String AGREEMENT_MAPPINGTEST = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"MAPPING_VALUE\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isBusiness\":{\"V\":\"true\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";


}
