package se.tink.backend.common.repository.cassandra;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;

public class CassandraUserDemographicsRepositoryImpl implements CassandraUserDemographicsRepositoryCustom {

    @Autowired
    private CassandraOperations cassandraOperations;
    
    private static final String TABLE_NAME = "users_demographics";

    @Override
    public void truncate() {
        cassandraOperations.truncate(TABLE_NAME);
    }

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .column("age", DataType.cint())
                .column("campaign", DataType.text())                
                .column("city", DataType.text())
                .column("birth", DataType.text())
                .column("postalcode", DataType.text())
                .column("community", DataType.text())
                .column("country", DataType.text())
                .column("created", DataType.timestamp())
                .column("credentialscount", DataType.cint())
                .column("currentcategorization", DataType.bigint())
                .column("deleted", DataType.timestamp())
                .column("firstupdatedevent", DataType.timestamp())
                .column("flags", DataType.set(DataType.text()))
                .column("followitemcount", DataType.cint())
                .column("gender", DataType.text())
                .column("hasfacebook", DataType.cboolean())
                .column("haspassword", DataType.cboolean())
                .column("hashadtransactions", DataType.cboolean())
                .column("organic", DataType.cboolean())
                .column("income", DataType.bigint())
                .column("initialcategorization", DataType.bigint())
                .column("lastupdatedevent", DataType.timestamp())
                .column("market", DataType.text())
                .column("providers", DataType.set(DataType.text()))
                .column("taggedtransactioncount", DataType.cint())
                .column("transactioncount", DataType.cint())
                .column("uniquetagcount", DataType.cint())
                .column("validcleandataperiodscount", DataType.cint())
                .column("validcredentialscount", DataType.cint())
                .column("weeklyautherrorfrequency", DataType.cdouble())
                .column("weeklyerrorfrequency", DataType.cdouble())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);
        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

}
