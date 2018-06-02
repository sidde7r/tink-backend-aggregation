package se.tink.backend.system.queue.firehose;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.UUID;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.follow.FollowData;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.firehose.v1.models.Account;
import se.tink.backend.firehose.v1.models.Activity;
import se.tink.backend.firehose.v1.models.Category;
import se.tink.backend.firehose.v1.models.Credential;
import se.tink.backend.firehose.v1.models.FollowItem;
import se.tink.backend.firehose.v1.models.Period;
import se.tink.backend.firehose.v1.models.Resolution;
import se.tink.backend.firehose.v1.models.SignableOperation;
import se.tink.backend.firehose.v1.models.Statistic;
import se.tink.backend.firehose.v1.models.Transaction;
import se.tink.backend.firehose.v1.queue.FirehoseModelConverters;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class FirehoseModelConvertersTest {
    private static ModelMapper modelMapper;

    @BeforeClass
    public static void setUp() {
        modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setPropertyCondition(mappingContext -> mappingContext.getSource() != null);

        FirehoseModelConverters.addConverters(modelMapper);
    }

    @Test
    public void testCredentialMapping() throws Exception {
        Credentials from = new Credentials();
        from.setId(StringUtils.generateUUID());
        from.setProviderName("danskebank");
        from.setStatus(CredentialsStatus.AUTHENTICATING);
        from.setUserId(StringUtils.generateUUID());
        from.setType(CredentialsTypes.PASSWORD);
        from.setField("key", "value");

        Credential to = modelMapper.map(from, Credential.class);

        Assert.assertEquals(from.getId(), to.getId());
        Assert.assertEquals(from.getProviderName(), to.getProviderName());
        Assert.assertEquals(Credential.Status.STATUS_AUTHENTICATING, to.getStatus());
        Assert.assertEquals("", to.getStatusPayload());
        Assert.assertEquals(from.getUserId(), to.getUserId());
        Assert.assertEquals(Credential.Type.TYPE_PASSWORD, to.getType());
        Assert.assertEquals(from.getFields().size(), to.getFieldsMap().size());
        Assert.assertEquals(from.getField("key"), to.getFieldsMap().get("key"));
    }

    @Test
    public void testAccountMapping() throws Exception {
        se.tink.backend.core.Account from = new se.tink.backend.core.Account();
        from.setId(StringUtils.generateUUID());
        from.setUserId(StringUtils.generateUUID());
        from.setCredentialsId(StringUtils.generateUUID());
        from.setAccountNumber("1234-123456789");
        from.setBalance(545d);
        from.setExcluded(false);
        from.setFavored(true);
        from.setName("Salary account");
        from.setOwnership(0.5d);
        from.setType(AccountTypes.CREDIT_CARD);
        ImageUrls imageUrls = new ImageUrls();
        imageUrls.setBanner("banner");
        imageUrls.setIcon("icon");
        from.setImages(imageUrls);
        from.setClosed(true);

        Account to = modelMapper.map(from, Account.class);

        Assert.assertEquals(from.getId(), to.getId());
        Assert.assertEquals(from.getUserId(), to.getUserId());
        Assert.assertEquals(from.getCredentialsId(), to.getCredentialsId());
        Assert.assertEquals(from.getAccountNumber(), to.getAccountNumber());
        Assert.assertEquals(from.getBalance(), to.getBalance(), 0.0001d);
        Assert.assertEquals(from.isExcluded(), to.getExcluded());
        Assert.assertEquals(from.isFavored(), to.getFavored());
        Assert.assertEquals(from.getName(), to.getName());
        Assert.assertEquals(from.getOwnership(), to.getOwnership(), 0.0001d);
        Assert.assertEquals(Account.Type.TYPE_CREDIT_CARD, to.getType());
        Assert.assertEquals(from.isUserModifiedExcluded(), to.getUserModifiedExcluded());
        Assert.assertEquals(from.isUserModifiedName(), to.getUserModifiedName());
        Assert.assertEquals(from.isUserModifiedType(), to.getUserModifiedType());
        Assert.assertEquals(from.getImages().getBanner(), to.getBannerUrl());
        Assert.assertEquals(from.getImages().getIcon(), to.getIconUrl());
        Assert.assertEquals(from.isClosed(), to.getClosed());
    }

    @Test
    public void testFollowItemMapping() throws Exception {
        se.tink.backend.core.follow.FollowItem from = new se.tink.backend.core.follow.FollowItem();
        from.setId("id");
        from.setCriteria("criteria");
        from.setType(FollowTypes.EXPENSES);
        from.setName("name");
        from.setData(new FollowData());

        FollowItem to = modelMapper.map(from, FollowItem.Builder.class).build();

        Assert.assertEquals(from.getId(), to.getId());
        Assert.assertEquals(from.getCriteria(), to.getCriteria());
        Assert.assertEquals(from.getName(), to.getName());
        Assert.assertEquals(FollowItem.Type.TYPE_EXPENSES, to.getType());
    }

    @Test
    public void testPeriodMapping() {
        se.tink.libraries.date.Period from = new se.tink.libraries.date.Period();
        from.setClean(true);
        from.setResolution(ResolutionTypes.DAILY);
        from.setName("name");
        from.setStartDate(new Date(123));
        from.setEndDate(new Date(12345));

        Period to = modelMapper.map(from, Period.Builder.class).build();

        Assert.assertEquals(from.isClean(), to.getClean());
        Assert.assertEquals(Resolution.Type.TYPE_DAILY, to.getResolution());
        Assert.assertEquals(from.getName(), to.getName());
        Assert.assertEquals(from.getStartDate().getTime(), to.getStartDate());
        Assert.assertEquals(from.getEndDate().getTime(), to.getEndDate());
    }

    @Test
    public void testStatisticsMapping() throws Exception {
        se.tink.backend.core.Statistic from = new se.tink.backend.core.Statistic();
        from.setDescription(StringUtils.generateUUID());
        from.setPeriod("2017-01-02");
        from.setType(se.tink.backend.core.Statistic.Types.BALANCES_BY_ACCOUNT);
        from.setUserId(StringUtils.generateUUID());
        from.setValue(101.1);
        from.setPayload("Payload");

        Statistic to = modelMapper.map(from, Statistic.Builder.class).build();

        Assert.assertEquals(from.getDescription(), to.getDescription());
        Assert.assertEquals(from.getPeriod(), to.getPeriod());
        Assert.assertEquals(from.getType(), to.getType());
        Assert.assertEquals(from.getPayload(), to.getPayload());
        Assert.assertEquals(from.getUserId(), to.getUserId());
        Assert.assertEquals(from.getValue(), to.getValue(), 0.01);
    }

    @Test
    public void testSignableOperationMapping() {
        se.tink.backend.core.signableoperation.SignableOperation from = new se.tink.backend.core.signableoperation.SignableOperation();
        from.setCreated(DateUtils.parseDate("2017-05-01"));
        from.setStatus(SignableOperationStatuses.AWAITING_CREDENTIALS);
        from.setStatusDetailsKey(
                se.tink.backend.core.signableoperation.SignableOperation.StatusDetailsKey.BANKID_FAILED);
        from.setStatusMessage("The transfer has been sent to your bank.");
        from.setType(SignableOperationTypes.TRANSFER);
        from.setUnderlyingId(UUID.randomUUID());
        from.setUpdated(DateUtils.parseDate("2017-07-01"));
        from.setUserId(UUID.randomUUID());
        from.setCredentialsId(UUID.randomUUID());
        from.setSignableObject("signableObject");

        SignableOperation to = modelMapper.map(from, SignableOperation.Builder.class).build();

        Assert.assertEquals(from.getCreated().getTime(), to.getCreated());
        Assert.assertEquals(from.getUpdated().getTime(), to.getUpdated());
        Assert.assertEquals(SignableOperation.Status.STATUS_AWAITING_CREDENTIALS, to.getStatus());
        Assert.assertEquals(SignableOperation.Type.TYPE_TRANSFER, to.getType());
        Assert.assertEquals(from.getStatusMessage(), to.getStatusMessage());
        Assert.assertEquals(UUIDUtils.toTinkUUID(from.getUnderlyingId()), to.getUnderlyingId());
        Assert.assertEquals(UUIDUtils.toTinkUUID(from.getUserId()), to.getUserId());
        Assert.assertEquals(UUIDUtils.toTinkUUID(from.getCredentialsId()), to.getCredentialsId());
    }

    @Test
    public void testTransactionMapping() {
        se.tink.backend.core.Transaction from = new se.tink.backend.core.Transaction();
        from.setAccountId("accountId");
        from.setAmount(1000.);
        from.setCategory("categoryId", CategoryTypes.INCOME);
        from.setDate(DateUtils.parseDate("2017-06-01"));
        from.setDescription("description");
        from.setFormattedDescription("formattedDescription");
        from.setLastModified(DateUtils.parseDate("2017-05-01"));
        from.setOriginalAmount(1001);
        from.setNotes("notes");
        from.setOriginalDate(DateUtils.parseDate("2017-05-11"));
        from.setOriginalDescription("originalDescription");
        from.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID, "transferId");
        from.setPending(true);
        from.setTimestamp(1000);
        from.setType(TransactionTypes.PAYMENT);
        from.setUserId("userId");
        from.setMerchantId("merchantId");
        from.setUpcoming(true);

        Transaction to = modelMapper.map(from, Transaction.Builder.class).build();

        Assert.assertEquals(from.getAccountId(), to.getAccountId());
        Assert.assertEquals(from.getAmount(), to.getAmount(), 0.001);
        Assert.assertEquals(from.getCategoryId(), to.getCategoryId());
        Assert.assertEquals(Category.Type.TYPE_INCOME, to.getCategoryType());
        Assert.assertEquals(from.getDate().getTime(), to.getDate());
        Assert.assertEquals(from.getDescription(), to.getDescription());
        Assert.assertEquals(from.getFormattedDescription(), to.getFormattedDescription());
        Assert.assertEquals(from.getLastModified().getTime(), to.getLastModified());
        Assert.assertEquals(from.getOriginalAmount(), to.getOriginalAmount(), 0.001);
        Assert.assertEquals(from.getNotes(), to.getNotes());
        Assert.assertEquals(from.getOriginalDate().getTime(), to.getOriginalDate());
        Assert.assertEquals(from.getOriginalDescription(), to.getOriginalDescription());
        Assert.assertEquals(from.getPayloadSerialized(), to.getPayloadSerialized());
        Assert.assertEquals(from.isPending(), to.getPending());
        Assert.assertEquals(from.getTimestamp(), to.getTimestamp());
        Assert.assertEquals(Transaction.Type.TYPE_PAYMENT, to.getType());
        Assert.assertEquals(from.getUserId(), to.getUserId());
        Assert.assertEquals(from.getMerchantId(), to.getMerchantId());
        Assert.assertEquals(from.isUpcoming(), to.getUpcoming());
    }

    @Test
    public void testCredentialEnumMapping () {
        ImmutableSet<CredentialsStatus> nonMappedCredentialsStatuses = ImmutableSet.of(
                CredentialsStatus.UNCHANGED,
                CredentialsStatus.AWAITING_OTHER_CREDENTIALS_TYPE,
                CredentialsStatus.NOT_IMPLEMENTED_ERROR,
                CredentialsStatus.HINTED,
                CredentialsStatus.DELETED,
                CredentialsStatus.METRIC,
                CredentialsStatus.DISABLED);

        for (CredentialsStatus source : CredentialsStatus.values()) {
            Credential.Status destination = modelMapper.map(source, Credential.Status.class);
            Assert.assertNotNull(String.format("%s should not map to NULL", source), destination);

            if (!nonMappedCredentialsStatuses.contains(source)) {
                Assert.assertNotEquals(String.format("%s should not map to UNKNOWN", source), Credential.Status.STATUS_UNKNOWN, destination);
            }
        }

        for (CredentialsTypes source : CredentialsTypes.values()) {
            Credential.Type destination = modelMapper.map(source, Credential.Type.class);
            Assert.assertNotNull(String.format("%s should not map to NULL", source), destination);
            Assert.assertNotEquals(String.format("%s should not map to UNKNOWN", source), Credential.Type.TYPE_UNKNOWN, destination);
        }
    }

    @Test
    public void testAccountEnumMapping () {
        for (AccountTypes source : AccountTypes.values()) {
            Account.Type destination = modelMapper.map(source, Account.Type.class);
            Assert.assertNotNull(String.format("%s should not map to NULL", source), destination);
            Assert.assertNotEquals(String.format("%s should not map to UNKNOWN", source), Account.Type.TYPE_UNKNOWN, destination);
        }
    }

    @Test
    public void testTransactionMappingFromFirehoseToCore() {
        Transaction.Builder from = Transaction.newBuilder();
        from.setAccountId("accountId");
        from.setAmount(1000);
        from.setCategoryId("categoryId");
        from.setCategoryType(Category.Type.TYPE_INCOME);
        from.setDate(DateUtils.parseDate("2017-06-01").getTime());
        from.setDescription("description");
        from.setFormattedDescription("formattedDescription");
        from.setLastModified(DateUtils.parseDate("2017-05-01").getTime());
        from.setOriginalAmount(1001);
        from.setNotes("notes");
        from.setOriginalDate(DateUtils.parseDate("2017-05-11").getTime());
        from.setOriginalDescription("originalDescription");
        from.setPayloadSerialized(SerializationUtils.serializeToString(
                ImmutableMap.of(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID, "transferId")));
        from.setPending(true);
        from.setTimestamp(1000);
        from.setType(Transaction.Type.TYPE_PAYMENT);
        from.setUserId("userId");
        from.setMerchantId("merchantId");
        from.setUpcoming(true);

        // TODO: fix this so we can use the modelmapper of this class.
        // I opened an issue here: https://github.com/modelmapper/modelmapper/issues/267.
        ModelMapper modelMapper = new ModelMapper();
        FirehoseModelConverters.addConverters(modelMapper);

        se.tink.backend.core.Transaction to = modelMapper.map(from.build(), se.tink.backend.core.Transaction.class);

        Assert.assertEquals(from.getAccountId(), to.getAccountId());
        Assert.assertEquals(from.getAmount(), to.getAmount(), 0.001);
        Assert.assertEquals(from.getCategoryId(), to.getCategoryId());
        Assert.assertEquals(CategoryTypes.INCOME, to.getCategoryType());
        Assert.assertEquals(from.getDate(), to.getDate().getTime());
        Assert.assertEquals(from.getDescription(), to.getDescription());
        Assert.assertEquals(from.getFormattedDescription(), to.getFormattedDescription());
        Assert.assertEquals(from.getLastModified(), to.getLastModified().getTime());
        Assert.assertEquals(from.getOriginalAmount(), to.getOriginalAmount(), 0.001);
        Assert.assertEquals(from.getNotes(), to.getNotes());
        Assert.assertEquals(from.getOriginalDate(), to.getOriginalDate().getTime());
        Assert.assertEquals(from.getOriginalDescription(), to.getOriginalDescription());
        Assert.assertEquals(from.getPayloadSerialized(), to.getPayloadSerialized());
        Assert.assertEquals(from.getPending(), to.isPending());
        Assert.assertEquals(from.getTimestamp(), to.getTimestamp());
        Assert.assertEquals(TransactionTypes.PAYMENT, to.getType());
        Assert.assertEquals(from.getUserId(), to.getUserId());
        Assert.assertEquals(from.getMerchantId(), to.getMerchantId());
        Assert.assertEquals(from.getUpcoming(), to.isUpcoming());
    }

    @Test
    public void testSignableOperationMappingFromFirehoseToCore() {
        SignableOperation.Builder from = SignableOperation.newBuilder();
        from.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        from.setCreated(DateUtils.parseDate("2017-05-01").getTime());
        from.setStatus(SignableOperation.Status.STATUS_AWAITING_CREDENTIALS);
        from.setStatusMessage("The transfer has been sent to your bank.");
        from.setType(SignableOperation.Type.TYPE_TRANSFER);
        from.setUnderlyingId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        from.setUpdated(DateUtils.parseDate("2017-07-01").getTime());
        from.setUserId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        from.setCredentialsId(UUIDUtils.toTinkUUID(UUID.randomUUID()));

        se.tink.backend.core.signableoperation.SignableOperation to = modelMapper
                .map(from.build(), se.tink.backend.core.signableoperation.SignableOperation.class);

        Assert.assertEquals(from.getId(), UUIDUtils.toTinkUUID(to.getId()));
        Assert.assertEquals(from.getCreated(), to.getCreated().getTime());
        Assert.assertEquals(from.getUpdated(), to.getUpdated().getTime());
        Assert.assertEquals(SignableOperationStatuses.AWAITING_CREDENTIALS, to.getStatus());
        Assert.assertEquals(SignableOperationTypes.TRANSFER, to.getType());
        Assert.assertEquals(from.getStatusMessage(), to.getStatusMessage());
        Assert.assertEquals(from.getUnderlyingId(), UUIDUtils.toTinkUUID(to.getUnderlyingId()));
        Assert.assertEquals(from.getUserId(), UUIDUtils.toTinkUUID(to.getUserId()));
        Assert.assertEquals(from.getCredentialsId(), UUIDUtils.toTinkUUID(to.getCredentialsId()));
    }

    @Test
    public void testActivityMappingFromCoreToFirehose() {
        se.tink.backend.core.Activity coreActivity = new se.tink.backend.core.Activity();
        String userId = UUIDUtils.generateUUID();
        coreActivity.setUserId(userId);
        coreActivity.setId(UUIDUtils.generateUUID());
        coreActivity.setTitle("testTitle");
        coreActivity.setDate(new Date());
        coreActivity.setType("test-type");
        coreActivity.setMessage("Test message.");
        coreActivity.setKey("test-key-123");
        coreActivity.setImportance(0.8);

        se.tink.backend.core.Transaction content = new se.tink.backend.core.Transaction();
        content.setUserId(userId);
        content.setDate(DateUtils.addDays(new Date(), -1));
        content.setPending(false);
        content.setNotes("Test #notes");
        coreActivity.setContent(content);

        Activity activity = FirehoseModelConverters.fromCoreToFirehose(coreActivity);

        Assert.assertEquals(activity.getUserId(), coreActivity.getUserId());
        Assert.assertEquals(activity.getId(), coreActivity.getId());
        Assert.assertEquals(activity.getTitle(), coreActivity.getTitle());
        Assert.assertEquals(activity.getDate(), coreActivity.getDate().getTime());
        Assert.assertEquals(activity.getType(), coreActivity.getType());
        Assert.assertEquals(activity.getMessage(), coreActivity.getMessage());
        Assert.assertEquals(activity.getKey(), coreActivity.getKey());
        Assert.assertEquals(activity.getImportance(), coreActivity.getImportance(), 0.001);
        Assert.assertEquals(activity.getContent(), SerializationUtils.serializeToString(coreActivity.getContent()));
    }
}
