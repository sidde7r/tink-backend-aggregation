package se.tink.analytics.jobs;

import se.tink.analytics.AnalyticsContext;

import com.datastax.spark.connector.japi.CassandraRow;
import com.datastax.spark.connector.japi.rdd.CassandraJavaRDD;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class DemoJob {

    private AnalyticsContext sparkContext;

    public DemoJob(AnalyticsContext sparkContext) {
        this.sparkContext = sparkContext;
    }

    public static void main(String[] args) throws Exception {
        AnalyticsContext context = AnalyticsContext.build("PLACEHOLDER APPLICATION", args[0]);
        context.start();
        try {
            new DemoJob(context).run();
        } finally {
            context.stop();
        }
    }

    private void run() throws SQLException {
        CassandraJavaRDD<CassandraRow> rdd = sparkContext.getJavaFunc().cassandraTable("tink", "users_demographics");
        System.out.println("Number of users_demographics rows in Cassandra: " + rdd.count());
        System.out.println("Number of users in MySQL: " + getAllUniqueUserIds().size());
    }

    private Set<String> getAllUniqueUserIds() throws SQLException {
        Builder<String> userids = ImmutableSet.builder();

        Statement stmt = sparkContext.getMysqlConnection().createStatement();
        try {
            if (stmt.execute("SELECT id FROM users")) {
                ResultSet resultset = stmt.getResultSet();
                try {
                    while (resultset.next()) {
                        userids.add(resultset.getString(1));
                    }
                } finally {
                    resultset.close();
                }
            }
        } finally {
            stmt.close();
        }

        return userids.build();
    }

}
