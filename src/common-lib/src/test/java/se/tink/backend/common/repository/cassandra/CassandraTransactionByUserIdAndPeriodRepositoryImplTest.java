package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.swing.text.html.Option;
import org.elasticsearch.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.repository.cassandra.CassandraTransactionByUserIdAndPeriodRepositoryImpl;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraTransactionByUserIdAndPeriodRepositoryImplTest {
    private final CassandraTransactionByUserIdAndPeriodRepositoryImpl cassandraRepository =
            new CassandraTransactionByUserIdAndPeriodRepositoryImpl();
    private final String TABLE_NAME = cassandraRepository.TABLE_NAME;

    private List<String> generateIds() {
        Random r = new Random();
        int size = (r.nextInt() & Integer.MAX_VALUE % 10) + 1;
        List<String> ids = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            ids.add(i, StringUtils.generateUUID());
        }
        return ids;
    }

    private List<Integer> generatePeriods() {
        Random r = new Random();
        int size = (r.nextInt() & Integer.MAX_VALUE % 10) + 1;
        List<Integer> ids = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            ids.add(i, r.nextInt());
        }
        return ids;
    }

    private Select getBasicSelectQuery() {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        return select;
    }

    @Test
    public void testEmptyQuery() {
        Select actual = getBasicSelectQuery();

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }

    @Test
    public void testOptionalUserId() {
        Optional<String> userId = Optional.of(StringUtils.generateUUID());
        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId.get())));

        Select target = cassandraRepository.selectQueryBuilder(
                userId, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }

    @Test
    public void testOptionalTransactionId() {
        Optional<String> id = Optional.of(StringUtils.generateUUID());
        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.eq("id", UUIDUtils.fromTinkUUID(id.get())));

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), id, Optional.empty(), Optional.empty(), Optional.empty());

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }

    @Test
    public void testOptionalTransactionIds() {
        Optional<List<String>> ids = Optional.of(generateIds());
        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.in("id", ids.get()));

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), Optional.empty(), ids, Optional.empty(), Optional.empty());

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }
    @Test
    public void testOptionalPeriod() {
        Optional<Integer> period = Optional.of(new Random().nextInt());
        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.eq("period", period.get()));

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), Optional.empty(), Optional.empty(), period, Optional.empty());

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }
    @Test
    public void testOptionalInPeriods() {
        Optional<List<Integer>> inPeriods = Optional.of(generatePeriods());
        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.in("period", inPeriods.get()));

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), inPeriods);

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }

    @Test
    public void testSinglePeriodPriority() {
        Optional<List<Integer>> inPeriods = Optional.of(generatePeriods());
        Optional<Integer> period = Optional.of(new Random().nextInt());

        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.eq("period", period.get()));

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), Optional.empty(), Optional.empty(), period, inPeriods);

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }

    @Test
    public void testSingleTransactionIdPriority() {
        Optional<String> id = Optional.of(generateIds().get(0));
        Optional<List<String>> ids = Optional.of(generateIds());
        Select actual = getBasicSelectQuery();
        actual.where(QueryBuilder.eq("id", id.get()));

        Select target = cassandraRepository.selectQueryBuilder(
                Optional.empty(), id, ids, Optional.empty(), Optional.empty());

        Assert.assertEquals(actual.getQueryString(), target.getQueryString());
        Assert.assertEquals(actual.getConsistencyLevel(), target.getConsistencyLevel());
    }
}
