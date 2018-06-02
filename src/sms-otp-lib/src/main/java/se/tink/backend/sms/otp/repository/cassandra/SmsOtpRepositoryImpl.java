package se.tink.backend.sms.otp.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.sms.otp.core.SmsOtp;

public class SmsOtpRepositoryImpl implements SmsOtpRepositoryCustom {
    private static final String TABLE_NAME = "sms_otps";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        CreateTableSpecification tableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("phoneNumber", DataType.text())
                .column("created", DataType.timestamp())
                .column("verified", DataType.timestamp())
                .column("expire", DataType.timestamp())
                .column("code", DataType.text())
                .column("payload", DataType.text())
                .column("verificationAttempts", DataType.bigint())
                .column("status", DataType.text())
                .column("type", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }

    @Override
    public SmsOtp save(SmsOtp entity, Seconds ttl) {
        WriteOptions options = new WriteOptions();
        options.setTtl(ttl.getSeconds());

        return cassandraOperations.insert(entity, options);
    }
}
