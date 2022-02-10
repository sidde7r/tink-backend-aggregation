package se.tink.backend.aggregation.agents.balance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.BalanceType;
import se.tink.backend.aggregation.agents.balance.AccountsBalancesUpdater.Mode;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculatorSummary;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@Builder
public class AccountsBalanceUpdaterSummary {

    private static final Gson GSON =
            new GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Pair.class, new PairDeserializer())
                    .create();

    private final Mode mode;

    private final AccountTypes inputAccountType;
    private final BalanceType balanceTypeToCalculate;

    private final Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularBalances;
    private final BalanceCalculatorSummary balanceCalculatorSummary;

    private final BigDecimal buggyBalance;
    private final BigDecimal calculatedBalance;

    public String prettyPrint() {
        return "[BALANCE UPDATER]\n\n" + GSON.toJson(this);
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime> {

        @Override
        public JsonElement serialize(
                LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private static class PairDeserializer
            implements JsonSerializer<Pair<ExactCurrencyAmount, Instant>> {

        @Override
        public JsonElement serialize(Pair pair, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(pair.toString());
        }
    }
}
