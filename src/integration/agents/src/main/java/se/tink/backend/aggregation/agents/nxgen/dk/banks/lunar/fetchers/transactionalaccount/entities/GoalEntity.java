package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.LunarPredicates;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@JsonObject
@Data
public class GoalEntity extends BaseResponseEntity {

    private static final String TITLE_IDENTIFIER = "title";
    private static final String TEXT_FIELD = "text";

    private BigDecimal balanceAmount;
    private String balanceCurrency;
    private Boolean cashedOut;
    private List<FieldEntity> fields;

    public List<FieldEntity> getFields() {
        return ListUtils.emptyIfNull(fields);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTransactionalAccount() {
        if (BooleanUtils.isTrue(cashedOut)) {
            log.info("Lunar goal was cashed out");
        }
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(buildBalanceModule())
                .withId(buildIdModule())
                .setApiIdentifier(id)
                .setBankIdentifier(id)
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
                        .filter(LunarPredicates.notDeleted())
                        .filter(this::isTitleField)
                        .map(FieldEntity::getValue)
                        .collect(Collectors.toList());
        if (titles.size() > 1) {
            log.info("Lunar goal has more than one title! Titles: {}", titles);
        } else if (titles.isEmpty()) {
            log.info("Lunar goal has no title!}");
            return "";
        }
        return titles.get(0);
    }

    private boolean isTitleField(FieldEntity field) {
        return TITLE_IDENTIFIER.equalsIgnoreCase(field.getFieldIdentifier())
                && TEXT_FIELD.equalsIgnoreCase(field.getType())
                && BooleanUtils.isNotFalse(field.getVisible());
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
