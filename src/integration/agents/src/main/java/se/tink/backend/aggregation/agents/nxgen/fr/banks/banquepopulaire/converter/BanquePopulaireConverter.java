package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.account.AccountDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.common.TypeDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.transaction.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transaction.entity.TransactionResponseEntity;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RequiredArgsConstructor
public class BanquePopulaireConverter {

    private final ObjectMapper objectMapper;

    public TransactionalAccount convertAccountDtoToTinkTransactionalAccount(AccountDto accountDto) {
        final ExactCurrencyAmount amount = convertBalanceDtoToTinkAmount(accountDto.getBalance());
        final String number = accountDto.getExternalReference();

        return TransactionalAccount.builder(getTinkAccountType(accountDto), number, amount)
                .setName(accountDto.getProductId().getLabel())
                .setAccountNumber(number)
                .setHolderName(new HolderName(accountDto.getClient().getDescriptionClient()))
                .setBankIdentifier(createTransactionalAccountBankIdentifier(accountDto))
                .build();
    }

    public TransactionResponseEntity convertTransactionDtoListToTransactionResponseEntity(
            List<TransactionDto> transactionDtos, String nextKey) {
        final List<Transaction> transactions =
                transactionDtos.stream()
                        .map(BanquePopulaireConverter::convertTransactionDtoToTinkTransaction)
                        .collect(Collectors.toList());

        return new TransactionResponseEntity(transactions, nextKey);
    }

    public <T> List<T> convertHttpResponseBodyToList(HttpResponse httpResponse, Class<T> clazz) {
        final JavaType type =
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);

        return objectMapper.convertValue(httpResponse.getBody(List.class), type);
    }

    private static Transaction convertTransactionDtoToTinkTransaction(
            TransactionDto transactionDto) {
        return Transaction.builder()
                .setDescription(getTransactionDescription(transactionDto))
                .setDate(
                        Instant.ofEpochMilli(transactionDto.getTransactionTimestamp())
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate())
                .setAmount(convertBalanceDtoToTinkAmount(transactionDto.getBalance()))
                .setPending(isPending(transactionDto))
                .build();
    }

    private static AccountTypes getTinkAccountType(AccountDto accountDto) {
        return BanquePopulaireConstants.Account.toTinkAccountType(
                accountDto.getContractType().getCode());
    }

    private static String createTransactionalAccountBankIdentifier(AccountDto accountDto) {
        return String.format(
                "%s-%s-%s",
                accountDto.getContractId().getBankCode(),
                accountDto.getContractType().getCode(),
                accountDto.getContractId().getIdentifier());
    }

    private static String getTransactionDescription(TransactionDto transactionDto) {
        return BanquePopulaireConstants.Fetcher.CARD_TRANSACTION_DESCRIPTION_PATTERN
                        .matcher(transactionDto.getTransactionLabel())
                        .matches()
                ? transactionDto.getSecondTransactionLabel()
                : transactionDto.getTransactionLabel();
    }

    private static boolean isPending(TransactionDto transactionDto) {
        final TypeDto transactionStatus = transactionDto.getTransactionStatus();

        if (!BanquePopulaireConstants.Status.TRANSACTION_STATUS_MAPPER.containsKey(
                transactionStatus.getCode())) {
            log.error(
                    BanquePopulaireConstants.LogTags.UNKNOWN_TRANSACTION_STATUS.toString()
                            + "  "
                            + SerializationUtils.serializeToString(transactionStatus));
        }

        return BanquePopulaireConstants.Status.TRANSACTION_STATUS_MAPPER.getOrDefault(
                transactionStatus.getCode(), false);
    }

    private static ExactCurrencyAmount convertBalanceDtoToTinkAmount(BalanceDto balanceDto) {
        return ExactCurrencyAmount.of(
                AgentParsingUtils.parseAmount(balanceDto.getAmount()),
                BanquePopulaireConstants.Currency.toTinkCurrency(balanceDto.getCurrency()));
    }
}
