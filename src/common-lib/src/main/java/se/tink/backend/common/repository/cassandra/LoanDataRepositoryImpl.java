package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Loan;

public class LoanDataRepositoryImpl implements LoanDataRepositoryCustom {

    private static final String TABLE_NAME = "loan_data";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification loanDataTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("accountid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid(), Ordering.DESCENDING)
                .column("userid", DataType.uuid())
                .column("credentialsid", DataType.uuid())
                .column("initialbalance", DataType.cdouble())
                .column("initialdate", DataType.timestamp())
                .column("nummonthsbound", DataType.cint())
                .column("name", DataType.text())
                .column("interest", DataType.cdouble())
                .column("balance", DataType.cdouble())
                .column("amortized", DataType.cdouble())
                .column("nextdayoftermschange", DataType.timestamp())
                .column("resolved", DataType.cboolean())
                .column("type", DataType.text())
                .column("providername", DataType.text())
                .column("serializedloanresponse", DataType.text())
                .column("loannumber", DataType.text())
                .column("monthlyamortization", DataType.cdouble())
                .column("usermodifiedtype", DataType.cboolean())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(loanDataTableSpec);
    }

    @Override
    public void deleteByAccountId(UUID accountId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("accountid", accountId));
        cassandraOperations.execute(delete);
    }

    @Override
    public Loan findLeastRecentOneByAccountId(String accountId) {
        return findFirstByAccountId(accountId);
    }

    private Loan findFirstByAccountId(String accountId) {
        List<Loan> allLoans = findAllByAccountId(accountId);
        return Iterables.getLast(allLoans, null);
    }
    
    public Loan findMostRecentOneByAccountId(String accountId) {
        return findMostRecentOneByAccountId(UUIDUtils.fromTinkUUID(accountId));
    }

    @Override
    public Loan findMostRecentOneByAccountId(UUID accountId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("accountid", accountId));
        select.limit(1);

        return cassandraOperations.selectOne(select, Loan.class);
    }

    @Override
    public List<Loan> findMostRecentByAccountId(UUID accountId, int limit) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("accountid", accountId));
        select.limit(limit);

        return cassandraOperations.select(select, Loan.class);
    }

    @Override
    public List<Loan> findAllByAccountId(String accountId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("accountid", UUIDUtils.fromTinkUUID(accountId)));
        return cassandraOperations.select(select, Loan.class);
    }

    @Override
    public ImmutableListMultimap<String, Loan> findAllByAccounts(List<Account> accounts) {
        List<Loan> loans = Lists.newArrayList();
        for(Account account : accounts) {
            loans.addAll(findAllByAccountId(account.getId()));
        }
        return Multimaps.index(loans, new Function<Loan, String>() {
            @Nullable
            @Override
            public String apply(Loan loan) {
                return UUIDUtils.toTinkUUID(loan.getAccountId());
            }
        });
    }

    @Override
    public boolean hasBeenUpdated(Loan loan) {
        Loan lastInsertedLoan = findMostRecentOneByAccountId(loan.getAccountId());

        return lastInsertedLoan == null || loan.hasUpdatedSince(lastInsertedLoan);
    }
}
