package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class StarlingTransactionMapperTest {

    @Test
    public void shouldMapTransaction() {
        // given
        TransactionEntity transactionEntity = createTransactionEntity();

        // when
        Transaction transaction = StarlingTransactionMapper.toTinkTransaction(transactionEntity);

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction).usingRecursiveComparison().isEqualTo(getExpectedTransaction());
    }

    private Transaction getExpectedTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(-15.72, "GBP"))
                        .setDescription("MARKET CAFE LONDON  E8 4  GBR")
                        .setPending(false)
                        .setDate(Date.from(Instant.parse("2020-08-05T00:00:00.000Z")))
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setTransactionDate(
                                                new AvailableDateInformation()
                                                        .setDate(LocalDate.parse("2020-08-04"))
                                                        .setInstant(
                                                                Instant.parse(
                                                                        "2020-08-04T11:49:16.421Z")))
                                        .setBookingDate(
                                                new AvailableDateInformation()
                                                        .setDate(LocalDate.parse("2020-08-05"))
                                                        .setInstant(
                                                                Instant.parse(
                                                                        "2020-08-05T00:00:00.000Z")))
                                        .build())
                        .setProviderMarket("UK")
                        .setType(TransactionTypes.DEFAULT)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                "11111a24-d2e9-48a8-a396-482a419cc273")
                        .setMerchantName("Market Cafe")
                        .setProprietaryFinancialInstitutionType("MASTER_CARD")
                        .build();
    }

    private TransactionEntity createTransactionEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"feedItemUid\": \"11111a24-d2e9-48a8-a396-482a419cc273\",\n"
                        + "\"categoryUid\": \"5d3c6070-1972-4f93-8f2a-44c0ccce5719\",\n"
                        + "\"amount\": {\n"
                        + "  \"currency\": \"GBP\",\n"
                        + "  \"minorUnits\": 1572\n"
                        + "},\n"
                        + "\"sourceAmount\": {\n"
                        + "  \"currency\": \"GBP\",\n"
                        + "  \"minorUnits\": 1572\n"
                        + "},\n"
                        + "\"direction\": \"OUT\",\n"
                        + "\"updatedAt\": \"2020-08-05T15:21:33.023Z\",\n"
                        + "\"transactionTime\": \"2020-08-04T11:49:16.421Z\",\n"
                        + "\"settlementTime\": \"2020-08-05T00:00:00.000Z\",\n"
                        + "\"source\": \"MASTER_CARD\",\n"
                        + "\"sourceSubType\": \"APPLE_PAY\",\n"
                        + "\"status\": \"UPCOMING\",\n"
                        + "\"transactingApplicationUserUid\": \"1f1c43dd-88de-4b10-b9ef-1a927eba81e1\",\n"
                        + "\"counterPartyType\": \"MERCHANT\",\n"
                        + "\"counterPartyUid\": \"20eac72c-8fc4-4060-9cbc-8b44091f83ba\",\n"
                        + "\"counterPartyName\": \"Market Cafe\",\n"
                        + "\"counterPartySubEntityUid\": \"9ae98f9c-1b4b-4bc6-b6a3-d38b7f6c1771\",\n"
                        + "\"reference\": \"MARKET CAFE LONDON  E8 4  GBR\",\n"
                        + "\"country\": \"GB\",\n"
                        + "\"spendingCategory\": \"EATING_OUT\",\n"
                        + "\"hasAttachment\": false\n"
                        + "}",
                TransactionEntity.class);
    }
}
