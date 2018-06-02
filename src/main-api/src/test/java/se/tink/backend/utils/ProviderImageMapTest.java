package se.tink.backend.utils;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Giro;
import se.tink.backend.core.ProviderImage;
import se.tink.backend.core.SwedishGiroType;

public class ProviderImageMapTest {
    List<ProviderImage> list1;
    List<ProviderImage> list2;

    static ProviderImage create(String code, String url) {
        ProviderImage image = new ProviderImage();
        image.setCode(code);
        image.setUrl(url);
        return image;
    }

    static Giro create(SwedishGiroType type, String name) {
        Giro giro = new Giro();
        giro.setType(type);
        giro.setName(name);
        return giro;
    }

    @Before
    public void setUp() {
        list1 = Lists.newArrayList();
        list1.add(create("icon:seb.account.savings", "url:icon:seb.account.savings"));
        list1.add(create("icon:seb.account.mortgage", "url:icon:seb.account.credit_card"));
        list1.add(create("icon:seb.account.loan", "url:icon:seb.account.loan"));
        list1.add(create("icon:seb", "url:icon:seb"));
        list1.add(create("icon:default", "url:icon:default"));
        list1.add(create("banner:default", "url:banner:default"));
        list1.add(create("icon:nordea", "url:icon:nordea"));
        list1.add(create("icon:seb.account", "url:icon:seb.account"));
        list1.add(create("icon:se-pg.klarna", "url:icon:se-pg.klarna"));
        list1.add(create("icon:se-bg", "url:icon:se-bg"));
        list1.add(create("icon:se-pg", "url:icon:se-pg"));
        list1.add(create("banner:seb.account", "url:banner:seb.account"));

        list2 = Lists.newArrayList();
    }

    @Test
    public void testDirectHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:seb", tree.find(ProviderImage.Type.ICON, "seb").getUrl());
        Assert.assertEquals("url:icon:nordea", tree.find(ProviderImage.Type.ICON, "nordea").getUrl());
    }

    @Test
    public void testDirectParentHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:seb.account.loan", tree.find(ProviderImage.Type.ICON, "seb.account.loan.test").getUrl());
    }

    @Test
    public void testLastParentHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:seb", tree.find(ProviderImage.Type.ICON, "seb.transfer.destination.external.hope").getUrl());
    }

    @Test
    public void testParentBgHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:se-bg", tree.find(ProviderImage.Type.ICON, create(SwedishGiroType.BG, "Something")).getUrl());
    }

    @Test
    public void testDirectPgHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:se-pg.klarna", tree.find(ProviderImage.Type.ICON, create(SwedishGiroType.PG, "Klarna")).getUrl());
    }

    @Test
    public void testDirectPgMiss() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:se-pg", tree.find(ProviderImage.Type.ICON, create(SwedishGiroType.PG, "Klarxyzna")).getUrl());
    }

    @Test
    public void testMidParentHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:seb.account", tree.find(ProviderImage.Type.ICON, "seb.account.blancoloan").getUrl());
        Assert.assertEquals("url:banner:seb.account", tree.find(ProviderImage.Type.BANNER, "seb.account.blancoloan").getUrl());
    }

    @Test
    public void testDefaultHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:default", tree.find(ProviderImage.Type.ICON, "tjoföljt.hejsan").getUrl());
    }

    @Test
    public void testDefaultHitBanner() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:banner:default", tree.find(ProviderImage.Type.BANNER, "tjoföljt.hejsan").getUrl());
    }


    @Test
    public void testNullDefaultHit() {
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:default", tree.find(ProviderImage.Type.ICON, (String) null).getUrl());
    }

    @Test
    public void testNonExistingInEmptyListShouldNotBeNull() {
        ProviderImageMap tree = new ProviderImageMap(list2, new ClearingNumberBankToProviderMapImpl());
        ProviderImage image = tree.find(ProviderImage.Type.ICON, "something");
        Assert.assertNotNull(image);
        Assert.assertEquals(null, image.getUrl());
    }

    @Test
    public void testDirectHitAccountType() {
        Account account = new Account();
        account.setType(AccountTypes.SAVINGS);
        ProviderImageMap tree = new ProviderImageMap(list1, new ClearingNumberBankToProviderMapImpl());
        Assert.assertEquals("url:icon:seb.account.savings",
                tree.getImagesForAccount("seb-bankid", account).getIcon());
    }
}