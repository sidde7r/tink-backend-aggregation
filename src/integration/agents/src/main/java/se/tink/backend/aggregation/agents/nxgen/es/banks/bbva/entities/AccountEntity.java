package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaPredicates.IS_TRANSACTIONAL_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Predicates;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.HolderTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountEntity extends AbstractContractDetailsEntity {

    private AmountEntity currentBalance;

    private AmountEntity availableBalanceLocalCurrency;
    private AmountEntity availableBalance;
    private AmountEntity currentBalanceLocalCurrency;

    public AmountEntity getCurrentBalance() {
        return currentBalance;
    }

    public AmountEntity getAvailableBalanceLocalCurrency() {
        return availableBalanceLocalCurrency;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public AmountEntity getCurrentBalanceLocalCurrency() {
        return currentBalanceLocalCurrency;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkTransactionalAccount(
            List<ParticipantAccountEntity> participantAccountEntities) {
        String iban = getAccountNumber();
        String accountProductId = getAccountProductId();

        if (Strings.isNullOrEmpty(iban) && Strings.isNullOrEmpty(accountProductId)) {
            return Optional.empty();
        }

        final AccountIdentifier ibanIdentifier = AccountIdentifier.create(Type.IBAN, iban);
        final DisplayAccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();
        final String formattedIban = ibanIdentifier.getIdentifier(formatter);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(ACCOUNT_TYPE_MAPPER, accountProductId)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban.toUpperCase(Locale.ENGLISH))
                                .withAccountNumber(formattedIban)
                                .withAccountName(getAccountName())
                                .addIdentifier(ibanIdentifier)
                                .build())
                .setApiIdentifier(getId())
                .addHolders(getHolders(participantAccountEntities))
                .build();
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return Option.ofOptional(ACCOUNT_TYPE_MAPPER.translate(getAccountProductId()))
                .filter(IS_TRANSACTIONAL_ACCOUNT)
                .isDefined();
    }

    @JsonIgnore
    public boolean hasBalance() {
        return Objects.nonNull(availableBalance)
                && Try.of(availableBalance::toTinkAmount).isSuccess();
    }

    @JsonIgnore
    @Override
    public String getAccountNumber() {
        return Optional.ofNullable(getFormats())
                .map(FormatsEntity::getIban)
                .filter(Predicates.not(Strings::isNullOrEmpty))
                .orElse(null);
    }

    @JsonIgnore
    public String getAccountProductId() {
        return Optional.ofNullable(getProduct())
                .map(ProductEntity::getId)
                .filter(Predicates.not(Strings::isNullOrEmpty))
                .orElse(null);
    }

    @JsonIgnore
    public List<Holder> getHolders(List<ParticipantAccountEntity> participantAccountEntities) {
        if (isOneAccountHolder(participantAccountEntities)) {
            return participantAccountEntities.stream()
                    .map(this::createHolder)
                    .collect(Collectors.toList());
        }

        log.info(
                "Account has {} owners {}",
                participantAccountEntities.size(),
                participantAccountEntities.stream()
                        .map(ParticipantAccountEntity::getRelationship)
                        .map(RelationshipEntity::getType)
                        .map(
                                typeEntity ->
                                        "id: "
                                                + typeEntity.getId()
                                                + " name: "
                                                + typeEntity.getName())
                        .collect(Collectors.toList()));

        List<RelationshipEntity> accountRelationships =
                getParticipants()
                        .map(ParticipantEntity::getRelationship)
                        .collect(Collectors.toList());

        return participantAccountEntities.stream()
                .filter(
                        participantAccountEntity ->
                                accountRelationships.stream()
                                        .allMatch(
                                                relationshipEntity ->
                                                        relationshipEntity
                                                                .getType()
                                                                .getId()
                                                                .equals(
                                                                        participantAccountEntity
                                                                                .getRelationship()
                                                                                .getType()
                                                                                .getId())))
                .map(this::createHolder)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private Holder createHolder(ParticipantAccountEntity participant) {
        switch (participant.getRoleId()) {
            case HolderTypes.OWNER:
                return Holder.of(
                        participant.getName() + " " + participant.getLastName(), Role.HOLDER);
            case HolderTypes.AUTHORIZED:
                return Holder.of(
                        participant.getName() + " " + participant.getLastName(),
                        Role.AUTHORIZED_USER);
            case HolderTypes.REPRESENTATIVE:
            default:
                return Holder.of(
                        participant.getName() + " " + participant.getLastName(), Role.OTHER);
        }
    }

    @JsonIgnore
    private boolean isOneAccountHolder(List<ParticipantAccountEntity> participantAccountEntities) {
        return participantAccountEntities.size() == 1;
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return availableBalance.toTinkAmount();
    }
}
