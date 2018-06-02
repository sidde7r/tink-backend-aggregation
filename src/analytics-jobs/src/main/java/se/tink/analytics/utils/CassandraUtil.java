package se.tink.analytics.utils;

import com.datastax.spark.connector.japi.SparkContextJavaFunctions;
import java.util.Map;
import org.apache.spark.api.java.JavaRDD;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;

public class CassandraUtil<T> {

    private String keyspace;
    private final Class<T> type;

    public CassandraUtil(String keyspace, Class<T> type) {
        this.keyspace = keyspace;
        this.type = type;
    }

    public JavaRDD<T> read(SparkContextJavaFunctions javaFunctions, String table) {
        return javaFunctions.cassandraTable(keyspace, table, mapRowTo(type));
    }

    public JavaRDD<T> read(SparkContextJavaFunctions javaFunctions, String table, Map<String, String> columnMap) {
        return javaFunctions.cassandraTable(keyspace, table, mapRowTo(type, columnMap));
    }

    public void save(String table, JavaRDD<T> rdd) {
        javaFunctions(rdd).writerBuilder(keyspace, table, mapToRow(type)).saveToCassandra();
    }

    public void save(String table, Map<String, String> columnMap, JavaRDD<T> rdd) {
        javaFunctions(rdd).writerBuilder(keyspace, table, mapToRow(type, columnMap)).saveToCassandra();
    }
}