package se.tink.backend.aggregation.nxgen.core.to_system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountParty;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.entity.BusinessIdentifier;
import se.tink.backend.aggregation.nxgen.core.account.entity.BusinessIdentifierType;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.user.rpc.User;

public class AccountConverterTest {

    @Test
    public void toSystemAccountShouldProperlyMapAccountPartyFullLegalName() {
        // given
        User user = mock(User.class);
        Provider provider = mock(Provider.class);

        String fullLegalName = UUID.randomUUID().toString();
        Party party = new Party(null, fullLegalName, Party.Role.HOLDER);
        Account account = mock(Account.class);
        when(account.getExactBalance()).thenReturn(ExactCurrencyAmount.of(BigDecimal.ZERO, "EUR"));
        when(account.getParties()).thenReturn(Collections.singletonList(party));

        // when
        se.tink.backend.agents.rpc.Account systemAccount =
                AccountConverter.toSystemAccount(user, provider, account);

        // then
        assertNotNull(systemAccount);
        AccountHolder accountHolder = systemAccount.getAccountHolder();
        assertNotNull(accountHolder);
        List<AccountParty> accountParties = accountHolder.getIdentities();
        assertEquals(1, accountParties.size());
        AccountParty accountParty = accountParties.get(0);
        assertNotNull(accountParty);
        assertEquals(fullLegalName, accountParty.getFullLegalName());
    }

    @Test
    public void toSystemAccountShouldProperlyMapAccountPartyBusinessIdentifiers() {
        // given
        User user = mock(User.class);
        Provider provider = mock(Provider.class);

        BusinessIdentifier[] businessIdentifiers =
                Arrays.stream(BusinessIdentifierType.values())
                        .map(it -> new BusinessIdentifier(it, it.name()))
                        .toArray(BusinessIdentifier[]::new);

        Party party =
                new Party(null, Party.Role.HOLDER).withBusinessIdentifiers(businessIdentifiers);
        Account account = mock(Account.class);
        when(account.getExactBalance()).thenReturn(ExactCurrencyAmount.of(BigDecimal.ZERO, "EUR"));
        when(account.getParties()).thenReturn(Collections.singletonList(party));

        List<se.tink.backend.agents.rpc.BusinessIdentifier> expectedBusinessIdentifiers =
                Arrays.stream(se.tink.backend.agents.rpc.BusinessIdentifierType.values())
                        .map(it -> new se.tink.backend.agents.rpc.BusinessIdentifier(it, it.name()))
                        .collect(Collectors.toList());

        // when
        se.tink.backend.agents.rpc.Account systemAccount =
                AccountConverter.toSystemAccount(user, provider, account);

        // then
        assertNotNull(systemAccount);
        AccountHolder accountHolder = systemAccount.getAccountHolder();
        assertNotNull(accountHolder);
        List<AccountParty> accountParties = accountHolder.getIdentities();
        assertEquals(1, accountParties.size());
        AccountParty accountParty = accountParties.get(0);
        assertNotNull(accountParty);
        assertEquals(expectedBusinessIdentifiers, accountParty.getBusinessIdentifiers());
    }
}
