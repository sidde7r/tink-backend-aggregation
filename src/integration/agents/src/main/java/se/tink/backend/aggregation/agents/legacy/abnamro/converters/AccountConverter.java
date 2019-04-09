package se.tink.backend.aggregation.agents.abnamro.converters;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.abnamro.client.model.PfmContractEntity;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroUtils;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountTypesMapper;

public class AccountConverter {
    public List<Account> convert(List<PfmContractEntity> contracts) {
        return contracts.stream().map(this::toAccount).collect(Collectors.toList());
    }

    private Account toAccount(PfmContractEntity contract) {
        Account account = new Account();
        account.setBankId(contract.getContractNumber());
        account.setName(contract.getName());
        account.setType(
                CoreAccountTypesMapper.toAggregation(
                        AbnAmroUtils.getAccountType(contract.getProductGroup())));

        if (account.getType() == AccountTypes.CREDIT_CARD) {
            return toCreditCardAccount(account, contract);
        } else {
            return toNonCreditCardAccount(account, contract);
        }
    }

    private Account toCreditCardAccount(Account account, PfmContractEntity contract) {
        account.setName(contract.getName().replaceFirst("ABN AMRO\\s*", ""));
        account.setBankId(AbnAmroUtils.creditCardIdToAccountId(account.getBankId()));
        account.setAccountNumber(
                AbnAmroUtils.maskCreditCardContractNumber(contract.getContractNumber()));
        account.putPayload(
                AbnAmroUtils.ABN_AMRO_ICS_ACCOUNT_CONTRACT_PAYLOAD, contract.getContractNumber());

        return account;
    }

    private Account toNonCreditCardAccount(Account account, PfmContractEntity contract) {
        account.setName(contract.getName());
        account.setBalance(contract.getBalance().getAmount());
        account.setAccountNumber(AbnAmroUtils.prettyFormatIban(contract.getAccountNumber()));

        return account;
    }
}
