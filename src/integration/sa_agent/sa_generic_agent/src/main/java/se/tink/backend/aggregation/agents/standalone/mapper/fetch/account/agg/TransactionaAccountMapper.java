package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.TransferDestination;
import se.tink.backend.aggregation.agents.standalone.mapper.common.GoogleDateMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.TransactionalAccount;

public class TransactionaAccountMapper
        implements Mapper<Account, se.tink.sa.services.fetch.account.TransactionalAccount> {

    private AccountExclusionMapper accountExclusionMapper;

    private ExactCurrencyAmountMapper exactCurrencyAmountMapper;

    private GoogleDateMapper googleDateMapper;

    private AccountTypesMapper accountTypesMapper;

    private AccountIdentifierMapper accountIdentifierMapper;

    private TransferDestinationMapper transferDestinationMapper;

    private AccountDetailsMapper accountDetailsMapper;

    private AccountFlagMapper accountFlagMapper;

    public void setAccountExclusionMapper(AccountExclusionMapper accountExclusionMapper) {
        this.accountExclusionMapper = accountExclusionMapper;
    }

    public void setExactCurrencyAmountMapper(ExactCurrencyAmountMapper exactCurrencyAmountMapper) {
        this.exactCurrencyAmountMapper = exactCurrencyAmountMapper;
    }

    public void setGoogleDateMapper(GoogleDateMapper googleDateMapper) {
        this.googleDateMapper = googleDateMapper;
    }

    public void setAccountTypesMapper(AccountTypesMapper accountTypesMapper) {
        this.accountTypesMapper = accountTypesMapper;
    }

    public void setAccountIdentifierMapper(AccountIdentifierMapper accountIdentifierMapper) {
        this.accountIdentifierMapper = accountIdentifierMapper;
    }

    public void setTransferDestinationMapper(TransferDestinationMapper transferDestinationMapper) {
        this.transferDestinationMapper = transferDestinationMapper;
    }

    public void setAccountDetailsMapper(AccountDetailsMapper accountDetailsMapper) {
        this.accountDetailsMapper = accountDetailsMapper;
    }

    public void setAccountFlagMapper(AccountFlagMapper accountFlagMapper) {
        this.accountFlagMapper = accountFlagMapper;
    }

    @Override
    public Account map(TransactionalAccount source, MappingContext mappingContext) {
        final Account account = new Account();
        account.setAccountNumber(source.getAccountNumber());
        account.setAccountExclusion(
                accountExclusionMapper.map(source.getAccountExclusion(), mappingContext));
        account.setAvailableCredit(source.getAvailableCredit());
        account.setExactAvailableCredit(
                exactCurrencyAmountMapper.map(source.getExactAvailableCredit(), mappingContext));
        account.setBalance(source.getBalance());
        account.setExactBalance(
                exactCurrencyAmountMapper.map(source.getExactBalance(), mappingContext));
        account.setCurrencyCode(source.getCurrencyCode());
        account.setBankId(source.getBankId());
        account.setCertainDate(googleDateMapper.map(source.getCertainDate(), mappingContext));
        account.setCredentialsId(source.getCredentialsId());
        account.setExcluded(source.getExcluded());
        account.setFavored(source.getFavored());
        account.setId(source.getId());
        account.setName(source.getName());
        account.setOwnership(source.getOwnership());
        account.setPayload(source.getPayload());
        account.setType(accountTypesMapper.map(source.getType(), mappingContext));
        account.setUserId(source.getUserId());
        account.setUserModifiedExcluded(source.getUserModifiedExcluded());
        account.setUserModifiedName(source.getUserModifiedName());
        account.setUserModifiedType(source.getUserModifiedType());
        account.setIdentifiers(mapAccountIdentifiers(source.getIdentifiersList(), mappingContext));
        account.setTransferDestinations(
                mapTransferDestinations(source.getTransferDestinationsList(), mappingContext));
        account.setDetails(accountDetailsMapper.map(source.getDetails(), mappingContext));
        account.setClosed(source.getClosed());
        account.setHolderName(source.getHolderName());
        account.setFlags(mapAccountFlags(source.getFlagsList(), mappingContext));
        account.setFinancialInstitutionId(source.getFinancialInstitutionId());
        return account;
    }

    private Collection<AccountIdentifier> mapAccountIdentifiers(
            final List<se.tink.sa.services.fetch.account.AccountIdentifier> accountIdentifiers,
            MappingContext mappingContext) {
        return Optional.ofNullable(accountIdentifiers).orElse(Collections.emptyList()).stream()
                .map(it -> accountIdentifierMapper.map(it, mappingContext))
                .collect(Collectors.toList());
    }

    private List<TransferDestination> mapTransferDestinations(
            final List<se.tink.sa.services.fetch.account.TransferDestination> transferDestinations,
            MappingContext mappingContext) {
        return Optional.ofNullable(transferDestinations).orElse(Collections.emptyList()).stream()
                .map(td -> transferDestinationMapper.map(td, mappingContext))
                .collect(Collectors.toList());
    }

    private Collection<AccountFlag> mapAccountFlags(
            final List<se.tink.sa.services.fetch.account.AccountFlag> accountFlags,
            MappingContext mappingContext) {
        return Optional.ofNullable(accountFlags).orElse(Collections.emptyList()).stream()
                .map(af -> accountFlagMapper.map(af, mappingContext))
                .collect(Collectors.toList());
    }
}
