package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class GoalEntity extends BaseResponseEntity {

    private static final Pattern TITLE_PATTERN = Pattern.compile("\"title\":\"(.+)\"");
    private static final String TITLE_IDENTIFIER = "title";
    private static final String TEXT_FIELD = "text";

    private BigDecimal balanceAmount;
    private String balanceCurrency;
    private List<FieldEntity> fields;

    public List<FieldEntity> getFields() {
        return ListUtils.emptyIfNull(fields);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTransactionalAccount(List<Party> accountHolders) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(buildBalanceModule())
                .withId(buildIdModule())
                .setApiIdentifier(id)
                .setBankIdentifier(id)
                .addParties(accountHolders)
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(id)
                .withAccountNumber(id)
                .withAccountName(StringUtils.replace(getTitle(), "\"", ""))
                .addIdentifier(new TinkIdentifier(id))
                .build();
    }

    private BalanceModule buildBalanceModule() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(balanceAmount, balanceCurrency))
                .setAvailableBalance(ExactCurrencyAmount.of(balanceAmount, balanceCurrency))
                .build();
    }

    private String getTitle() {
        // Wiski delete logs and refactor it after getting more data from logs
        List<String> titles =
                fields.stream()
                        .filter(BaseResponseEntity::notDeleted)
                        .filter(field -> BooleanUtils.isNotFalse(field.getVisible()))
                        .map(this::getTitleOrNull)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        if (titles.isEmpty()) {
            log.info("Lunar goal has no title!");
            return "";
        }
        return titles.get(0);
    }

    private String getTitleOrNull(FieldEntity field) {
        if (fieldIdentifierIsTitle(field)) {
            return field.getValue();
        } else if (StringUtils.isBlank(field.getFieldIdentifier())) {
            Matcher matcher = TITLE_PATTERN.matcher(field.getValue());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private boolean fieldIdentifierIsTitle(FieldEntity field) {
        return TITLE_IDENTIFIER.equalsIgnoreCase(field.getFieldIdentifier())
                && TEXT_FIELD.equalsIgnoreCase(field.getType());
    }

    @EqualsAndHashCode(callSuper = true)
    @JsonObject
    @Data
    private static class FieldEntity extends BaseResponseEntity {
        private String fieldIdentifier;
        private String type;
        private String value;
        private Boolean visible;
    }
}
