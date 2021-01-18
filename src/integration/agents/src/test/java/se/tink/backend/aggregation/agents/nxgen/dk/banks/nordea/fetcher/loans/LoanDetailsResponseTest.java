package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.LoansTestData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RunWith(JUnitParamsRunner.class)
public class LoanDetailsResponseTest {

    private static final String FIRST_ACCOUNT_ID = "ID 001";
    private static final String SECOND_ACCOUNT_ID = "ID123";
    private static final String PRODUCT_CODE = "FRBLÅN";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ObjectNode jsonResponseWithPropertiesAssumedToExistForTinkModelConversion =
            readObjectNodeFromFile(
                    LoansTestData
                            .LOAN_DETAILS_PROPERTIES_ASSUMED_TO_EXIST_FOR_TINK_MODEL_CONVERSION_FILE);

    @Test
    public void unknownPropertiesShouldBeIgnoredInDeserialization() {
        // given
        String responseAsJson =
                "{"
                        + "\"loanId\": \"ID12345\","
                        + "\"unknown_property\": \"unknown property value\""
                        + "}";

        // when
        LoanDetailsResponse response = readResponseFromJson(responseAsJson);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    public void noPropertiesShouldBeRequiredInDeserialization() {
        // given
        String responseAsJson = "{}";

        // when
        LoanDetailsResponse response = readResponseFromJson(responseAsJson);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    public void shouldAssumeOnlyAFewPropertiesHaveToExistForTinkModelConversion() {
        // given
        LoanDetailsResponse response =
                objectNodeToResponse(
                        jsonResponseWithPropertiesAssumedToExistForTinkModelConversion);

        // when
        LoanAccount loanAccount = response.toTinkLoanAccount();

        // then
        assertThat(loanAccount)
                .isEqualToComparingFieldByFieldRecursively(
                        LoanAccount.nxBuilder()
                                .withLoanDetails(
                                        LoanModule.builder()
                                                .withType(LoanDetails.Type.OTHER)
                                                .withBalance(
                                                        new ExactCurrencyAmount(
                                                                BigDecimal.valueOf(13000.4), null))
                                                .withInterestRate(0.041)
                                                .setApplicants(Collections.emptyList())
                                                .setCoApplicant(false)
                                                .setLoanNumber(FIRST_ACCOUNT_ID)
                                                .build())
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(FIRST_ACCOUNT_ID)
                                                .withAccountNumber("DK77 1234 1234 1234 12")
                                                .withAccountName("DK77 1234 1234 1234 12")
                                                .addIdentifier(
                                                        new DanishIdentifier(FIRST_ACCOUNT_ID))
                                                .setProductName(PRODUCT_CODE)
                                                .build())
                                .setApiIdentifier(
                                        NordeaDkConstants.PathValues.ACCOUNT_ID_PREFIX
                                                + FIRST_ACCOUNT_ID)
                                .putInTemporaryStorage(
                                        NordeaDkConstants.StorageKeys.PRODUCT_CODE, PRODUCT_CODE)
                                .build());
    }

    @Test
    @Parameters(method = "loanDetailsResponsesWithExpectedType")
    public void shouldConvertToCorrectLoanType(
            LoanDetailsResponse response, LoanDetails.Type expectedType) {
        // when
        LoanAccount loanAccount = response.toTinkLoanAccount();

        // then
        assertThat(loanAccount.getDetails().getType()).isEqualTo(expectedType);
    }

    private Object[] loanDetailsResponsesWithExpectedType() {
        return new Object[] {
            new Object[] {createResponseForLoanTypeTesting(null), LoanDetails.Type.OTHER},
            new Object[] {createResponseForLoanTypeTesting(""), LoanDetails.Type.OTHER},
            new Object[] {createResponseForLoanTypeTesting("235^#$^@#^"), LoanDetails.Type.OTHER},
            new Object[] {createResponseForLoanTypeTesting("other"), LoanDetails.Type.OTHER},
            new Object[] {createResponseForLoanTypeTesting("mortgage"), LoanDetails.Type.MORTGAGE}
        };
    }

    private LoanDetailsResponse createResponseForLoanTypeTesting(String groupPropertyValue) {
        ObjectNode jsonResponse =
                copyObjectNode(jsonResponseWithPropertiesAssumedToExistForTinkModelConversion);
        jsonResponse.put("group", groupPropertyValue);
        return objectNodeToResponse(jsonResponse);
    }

    @Test
    @Parameters(method = "loanDetailsResponsesWithExpectedAccountName")
    public void shouldPickAccountNameFromNicknameOrDefaultToAccountNumber(
            LoanDetailsResponse response, String expectedAccountName) {
        // when
        LoanAccount loanAccount = response.toTinkLoanAccount();

        // then
        assertThat(loanAccount.getName()).isEqualTo(expectedAccountName);
    }

    private Object[] loanDetailsResponsesWithExpectedAccountName() {
        return new Object[] {
            new Object[] {createResponseForAccountNameTesting("nickname1", "ID123"), "nickname1"},
            new Object[] {createResponseForAccountNameTesting("", "ID1234"), ""},
            new Object[] {createResponseForAccountNameTesting(null, "ID00"), "ID00"},
        };
    }

    private LoanDetailsResponse createResponseForAccountNameTesting(
            String nicknamePropertyValue, String loanFormattedIdPropertyValue) {
        ObjectNode jsonResponse =
                copyObjectNode(jsonResponseWithPropertiesAssumedToExistForTinkModelConversion);
        jsonResponse.put("nickname", nicknamePropertyValue);
        jsonResponse.put("loan_formatted_id", loanFormattedIdPropertyValue);
        return objectNodeToResponse(jsonResponse);
    }

    @Test
    public void shouldProperlyConvertToTinkAccount() {
        // given
        LoanDetailsResponse response =
                readResponseFromFile(
                        LoansTestData
                                .LOAN_DETAILS_WITH_ALL_PROPERTIES_RELEVANT_FOR_TINK_MODEL_FILE);

        // when
        LoanAccount loanAccount = response.toTinkLoanAccount();

        // then
        assertThat(loanAccount)
                .isEqualToComparingFieldByFieldRecursively(
                        LoanAccount.nxBuilder()
                                .withLoanDetails(
                                        LoanModule.builder()
                                                .withType(LoanDetails.Type.MORTGAGE)
                                                .withBalance(
                                                        new ExactCurrencyAmount(
                                                                BigDecimal.valueOf(-81469.41),
                                                                "DKK"))
                                                .withInterestRate(0.102500)
                                                .setAmortized(
                                                        new ExactCurrencyAmount(
                                                                BigDecimal.valueOf(495117.49),
                                                                "DKK"))
                                                .setInitialBalance(
                                                        new ExactCurrencyAmount(
                                                                BigDecimal.valueOf(2049000.0),
                                                                "DKK"))
                                                .setApplicants(
                                                        Arrays.asList("owner 1", "owner 2", ""))
                                                .setCoApplicant(true)
                                                .setLoanNumber("ID123")
                                                .setNextDayOfTermsChange(LocalDate.of(2021, 1, 1))
                                                .setSecurity("financed object name")
                                                .build())
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(SECOND_ACCOUNT_ID)
                                                .withAccountNumber("FORMATTED " + SECOND_ACCOUNT_ID)
                                                .withAccountName("account nickname")
                                                .addIdentifier(
                                                        new DanishIdentifier(SECOND_ACCOUNT_ID))
                                                .setProductName(PRODUCT_CODE)
                                                .build())
                                .setApiIdentifier(
                                        NordeaDkConstants.PathValues.ACCOUNT_ID_PREFIX
                                                + SECOND_ACCOUNT_ID)
                                .putInTemporaryStorage(
                                        NordeaDkConstants.StorageKeys.PRODUCT_CODE, PRODUCT_CODE)
                                .build());
    }

    @SneakyThrows
    private ObjectNode copyObjectNode(ObjectNode objectNode) {
        return objectMapper.readValue(objectNode.toString(), ObjectNode.class);
    }

    @SneakyThrows
    private LoanDetailsResponse objectNodeToResponse(ObjectNode objectNode) {
        String json = objectMapper.writeValueAsString(objectNode);
        return objectMapper.readValue(json, LoanDetailsResponse.class);
    }

    private LoanDetailsResponse readResponseFromJson(String json) {
        return SerializationUtils.deserializeFromString(json, LoanDetailsResponse.class);
    }

    private LoanDetailsResponse readResponseFromFile(String filePath) {
        return SerializationUtils.deserializeFromString(
                new File(filePath), LoanDetailsResponse.class);
    }

    @SneakyThrows
    private ObjectNode readObjectNodeFromFile(String filePath) {
        return objectMapper.readValue(new File(filePath), ObjectNode.class);
    }
}
