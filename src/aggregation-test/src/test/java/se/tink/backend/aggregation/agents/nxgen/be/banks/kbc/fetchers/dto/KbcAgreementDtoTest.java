package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;

public class KbcAgreementDtoTest {

    private static final String FAKE_IBAN = "BE62456595893861"; // http://randomiban.com/?country=Belgium

    private static final String AGREEMENT_NO_BUSINESS = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"3844\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isBusiness\":{\"V\":\"false\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"20170905233735244689\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";
    private static final String AGREEMENT_BUSINESS = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"3844\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isBusiness\":{\"V\":\"true\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"20170905233735244689\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";
    private static final String AGREEMENT_MISSING_BUSINESS_TAG = "{\"agreementNo\":{\"V\":\"" + FAKE_IBAN + "\",\"T\":\"ibanbban\"},\"structureCode\":{\"V\":\"IBN\",\"T\":\"text\"},\"productType\":{\"V\":\"KBCAccount\",\"T\":\"text\"},\"productTypeNr\":{\"V\":\"3844\",\"T\":\"text\"},\"agreementType\":{\"V\":\"01\",\"T\":\"text\"},\"agreementName\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"rubricName\":{\"V\":\"\",\"T\":\"text\"},\"balance\":{\"V\":\"50.76\",\"T\":\"decimal\"},\"currency\":{\"V\":\"EUR\",\"T\":\"text\"},\"counterValueBalance\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"counterValueCurrency\":{\"V\":\"\",\"T\":\"text\"},\"transactionsAvailable\":{\"V\":\"true\",\"T\":\"boolean\"},\"showBalance\":{\"V\":\"true\",\"T\":\"boolean\"},\"companyNo\":{\"V\":\"0001\",\"T\":\"text\"},\"roleCode\":{\"V\":\"T\",\"T\":\"text\"},\"statusCode\":{\"V\":\"1\",\"T\":\"text\"},\"canBeDebitted\":{\"V\":\"true\",\"T\":\"boolean\"},\"canBeCreditted\":{\"V\":\"false\",\"T\":\"boolean\"},\"executionDateAllowed\":{\"V\":\"true\",\"T\":\"boolean\"},\"isCompleted\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementMakeUp\":{\"name\":{\"V\":\"FIRSTNAME LASTNAME\",\"T\":\"text\"},\"partitionNumber\":{\"V\":\"01\",\"T\":\"text\"},\"pictureNumber\":{\"V\":\"20170905233735244689\",\"T\":\"text\"},\"picture\":{\"V\":\"\",\"T\":\"file\"}},\"visibilityIndicator\":{\"V\":\"true\",\"T\":\"boolean\"},\"principalAccountHolder\":{\"V\":\"8888888\",\"T\":\"text\"},\"balanceIncludingReservations\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"balanceIncludinReservationAmountEur\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationAmount\":{\"V\":\"0.00\",\"T\":\"decimal\"},\"reservationIndicator\":{\"V\":\"false\",\"T\":\"boolean\"},\"agreementStructuredMessage\":{\"V\":\"\",\"T\":\"text\"}}";

    @Test
    public void shouldNotSetBusinessFlag() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto agreementDto = objectMapper.readValue(AGREEMENT_NO_BUSINESS, se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        org.junit.Assert.assertTrue("No account flags should be set", transactionalAccount.getAccountFlags().isEmpty());
    }

    @Test
    public void shouldSetBusinessFlag() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto agreementDto = objectMapper.readValue(AGREEMENT_BUSINESS, se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        org.junit.Assert.assertTrue("Business flag set", transactionalAccount.getAccountFlags().get(0).equals(AccountFlag.BUSINESS));
    }

    @Test
    public void shouldHandleMissingBusinessFlag() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto agreementDto = objectMapper.readValue(AGREEMENT_MISSING_BUSINESS_TAG, se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto.class);
        TransactionalAccount transactionalAccount = agreementDto.toTransactionalAccount();

        org.junit.Assert.assertTrue("No account flags should be set", transactionalAccount.getAccountFlags().isEmpty());
    }
}
