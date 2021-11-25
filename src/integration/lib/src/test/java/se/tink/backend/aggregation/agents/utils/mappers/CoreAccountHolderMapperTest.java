package se.tink.backend.aggregation.agents.utils.mappers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountParty;
import se.tink.backend.agents.rpc.AccountPartyAddress;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountHolder;
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
}
