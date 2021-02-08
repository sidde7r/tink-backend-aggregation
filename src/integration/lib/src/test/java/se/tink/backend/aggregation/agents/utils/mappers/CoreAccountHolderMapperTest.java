package se.tink.backend.aggregation.agents.utils.mappers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountHolder;
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

        HolderIdentity hi1 = new HolderIdentity();
        hi1.setName("name1");
        hi1.setRole(HolderRole.AUTHORIZED_USER);

        HolderIdentity hi2 = new HolderIdentity();
        hi2.setName("name2");
        hi2.setRole(HolderRole.HOLDER);

        HolderIdentity hi3 = new HolderIdentity();
        hi3.setName("name3");
        hi3.setRole(HolderRole.OTHER);

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
    }
}
