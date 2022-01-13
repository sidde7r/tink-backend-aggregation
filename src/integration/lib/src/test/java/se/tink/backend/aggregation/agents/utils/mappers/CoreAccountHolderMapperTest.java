package se.tink.backend.aggregation.agents.utils.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountParty;
import se.tink.backend.agents.rpc.AccountPartyAddress;
import se.tink.backend.agents.rpc.BusinessIdentifier;
import se.tink.backend.agents.rpc.BusinessIdentifierType;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountHolder;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountHolderIdentity;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountPartyAddressType;
import se.tink.libraries.uuid.UUIDUtils;

public class CoreAccountHolderMapperTest {
    @Test
    public void allFieldsMappedFromAggregation() {
        CoreAccountHolderMapper.fromAggregationTypeMap.validate();
    }

    @Test
    public void nullIsMappableButReturnsEmpty() {
        Optional<AccountHolder> holder = CoreAccountHolderMapper.fromAggregation(null);
        Assert.assertFalse(holder.isPresent());
    }

    @Test
    public void mappableFromAggregation() {
        String accountId = UUIDUtils.generateUUID();

        se.tink.backend.agents.rpc.AccountHolder holder =
                new se.tink.backend.agents.rpc.AccountHolder();
        holder.setAccountId(accountId);
        holder.setType(AccountHolderType.BUSINESS);

        AccountParty hi1 = new AccountParty();
        hi1.setName("name1");
        hi1.setRole(HolderRole.AUTHORIZED_USER);

        AccountParty hi2 = new AccountParty();
        hi2.setName("name2");
        hi2.setRole(HolderRole.HOLDER);

        AccountParty hi3 = new AccountParty();
        hi3.setName("name3");
        hi3.setRole(HolderRole.OTHER);
        hi3.setAddresses(
                Collections.singletonList(
                        new AccountPartyAddress(
                                AccountPartyAddressType.ADDRESS_MAIL_TO,
                                "Brevlådan",
                                "78022",
                                "Överträsk",
                                "SE")));

        holder.setIdentities(Lists.newArrayList(hi1, hi2, hi3));

        AccountHolder acAccountHolder = CoreAccountHolderMapper.fromAggregation(holder).get();

        assertEquals(accountId, acAccountHolder.getAccountId());
        assertEquals("BUSINESS", acAccountHolder.getType().toString());

        assertEquals(3, acAccountHolder.getIdentities().size());
        assertEquals("name1", acAccountHolder.getIdentities().get(0).getName());
        assertEquals(
                "AUTHORIZED_USER", acAccountHolder.getIdentities().get(0).getRole().toString());
        assertEquals("name2", acAccountHolder.getIdentities().get(1).getName());
        assertEquals("HOLDER", acAccountHolder.getIdentities().get(1).getRole().toString());
        assertEquals("name3", acAccountHolder.getIdentities().get(2).getName());
        assertEquals("OTHER", acAccountHolder.getIdentities().get(2).getRole().toString());
        assertEquals(1, acAccountHolder.getIdentities().get(2).getAddresses().size());
        assertEquals(
                "Brevlådan",
                acAccountHolder.getIdentities().get(2).getAddresses().get(0).getStreet());
        assertEquals(
                AccountPartyAddressType.ADDRESS_MAIL_TO,
                acAccountHolder.getIdentities().get(2).getAddresses().get(0).getAddressType());
    }

    @Test
    public void fromAggregationShouldProperlyMapBusinessIdentifierTypes() {
        // given
        List<se.tink.backend.aggregationcontroller.v1.rpc.accountholder.BusinessIdentifier>
                expectedBusinessIdentifiers =
                        Arrays.stream(
                                        se.tink.backend.aggregationcontroller.v1.rpc.accountholder
                                                .BusinessIdentifierType.values())
                                .map(
                                        it ->
                                                se.tink.backend.aggregationcontroller.v1.rpc
                                                        .accountholder.BusinessIdentifier.of(
                                                        it, it.name()))
                                .collect(Collectors.toList());

        List<BusinessIdentifier> businessIdentifiers =
                Arrays.stream(BusinessIdentifierType.values())
                        .map(it -> new BusinessIdentifier(it, it.name()))
                        .collect(Collectors.toList());

        AccountParty accountParty = new AccountParty();
        accountParty.setBusinessIdentifiers(businessIdentifiers);

        se.tink.backend.agents.rpc.AccountHolder accountHolder =
                new se.tink.backend.agents.rpc.AccountHolder();
        accountHolder.setIdentities(Collections.singletonList(accountParty));

        // when
        Optional<AccountHolder> mappedAccountHolder =
                CoreAccountHolderMapper.fromAggregation(accountHolder);

        // then
        assertTrue(mappedAccountHolder.isPresent());
        List<AccountHolderIdentity> accountHolderIdentities =
                mappedAccountHolder.get().getIdentities();
        assertNotNull(accountHolderIdentities);
        assertEquals(1, accountHolderIdentities.size());
        AccountHolderIdentity accountHolderIdentity = accountHolderIdentities.get(0);
        assertEquals(expectedBusinessIdentifiers, accountHolderIdentity.getBusinessIdentifiers());
    }

    @Test
    public void fromAggregationShouldProperlyMapFullLegalName() {
        // given
        String fullLegalName = UUID.randomUUID().toString();

        AccountParty accountParty = new AccountParty();
        accountParty.setFullLegalName(fullLegalName);

        se.tink.backend.agents.rpc.AccountHolder accountHolder =
                new se.tink.backend.agents.rpc.AccountHolder();
        accountHolder.setIdentities(Collections.singletonList(accountParty));

        // when
        Optional<AccountHolder> mappedAccountHolder =
                CoreAccountHolderMapper.fromAggregation(accountHolder);

        // then
        assertTrue(mappedAccountHolder.isPresent());
        List<AccountHolderIdentity> accountHolderIdentities =
                mappedAccountHolder.get().getIdentities();
        assertNotNull(accountHolderIdentities);
        assertEquals(1, accountHolderIdentities.size());
        AccountHolderIdentity accountHolderIdentity = accountHolderIdentities.get(0);
        assertEquals(fullLegalName, accountHolderIdentity.getFullLegalName());
    }
}
