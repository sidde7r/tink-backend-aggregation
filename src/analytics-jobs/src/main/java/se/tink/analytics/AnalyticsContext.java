package se.tink.analytics;

import com.datastax.spark.connector.types.TypeConverter;
import com.datastax.spark.connector.types.TypeConverter$;
import se.tink.analytics.lifecycle.Managed;

import se.tink.analytics.config.MySQLConfiguration;
import se.tink.analytics.config.MySQLCredentials;
import com.datastax.bdp.spark.DseSparkConfHelper;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.datastax.spark.connector.japi.SparkContextJavaFunctions;
import com.google.common.base.Preconditions;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import se.tink.backend.analytics.spark.converters.CustomConverterFactory;

// A context held throughout the lifetime of a spark job execution.
public class AnalyticsContext implements Managed {

    private static final String MYSQL_USERNAME_PROPERTY = "mysql.username";
    private static final String MYSQL_PASSWORD_PROPERTY = "mysql.password";
    private static final String MYSQL_HOSTNAME_PROPERTY = "mysql.hostname";
    private static final String MYSQL_DATABASE_PROPERTY = "mysql.database";
    private static final String CASSANDRA_CONNECTION_HOST = "spark.cassandra.connection.host";

    public static AnalyticsContext build(String appName, String fileName) throws IOException {
        Properties prop = new Properties();
        FileReader propertyReader = new FileReader(fileName);
        try {
            prop.load(propertyReader);
        } finally {
            propertyReader.close();
        }

        SparkConf sparkConf = new SparkConf();

        String sparkCassandraConnectionHost = prop.getProperty(CASSANDRA_CONNECTION_HOST);

        // Will override any default values in $SPARK_HOME/conf/spark-default.conf
        if (sparkCassandraConnectionHost != null) {
            sparkConf.set(CASSANDRA_CONNECTION_HOST, sparkCassandraConnectionHost);
        }

        SparkConf dseSparkConf = DseSparkConfHelper.enrichSparkConf(sparkConf).setAppName(appName);

        JavaSparkContext sparkContext = new JavaSparkContext(dseSparkConf);
        SparkContextJavaFunctions javaFunc = CassandraJavaUtil.javaFunctions(sparkContext);

        // Initialize database.

        loadMySQLDriver();

        MySQLCredentials mysqlCredentials = new MySQLCredentials(Preconditions.checkNotNull(prop
                .getProperty(MYSQL_USERNAME_PROPERTY), "Username missing. Required."), Preconditions.checkNotNull(prop
                .getProperty(MYSQL_PASSWORD_PROPERTY), "Password missing. Required."));
        MySQLConfiguration mysqlConfiguration = new MySQLConfiguration(Preconditions.checkNotNull(prop
                .getProperty(MYSQL_HOSTNAME_PROPERTY), "Hostname missing. Required."), Preconditions.checkNotNull(prop
                .getProperty(MYSQL_DATABASE_PROPERTY), "Database name missing. Required."), mysqlCredentials);


        // Initialize spark custom converters
        initializeCustomConverters();

        return new AnalyticsContext(javaFunc, sparkContext, mysqlConfiguration);
    }

    // See https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-connect-drivermanager.html.
    private static void loadMySQLDriver() {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
    }

    private SparkContextJavaFunctions javaFunc;
    private JavaSparkContext sparkContext;
    private MySQLConfiguration mysqlConfiguration;
    private Connection mysqlConnection;

    private AnalyticsContext(SparkContextJavaFunctions javaFunc,
            JavaSparkContext sparkContext, MySQLConfiguration mysqlConfiguration) {
        this.javaFunc = javaFunc;
        this.sparkContext = sparkContext;
        this.mysqlConfiguration = mysqlConfiguration;
    }

    public SparkContextJavaFunctions getJavaFunc() {
        return javaFunc;
    }

    public JavaSparkContext getSparkContext() {
        return sparkContext;
    }

    @Override
    public void start() throws Exception {
        Preconditions.checkState(mysqlConnection == null, "There is already a connection.");

        mysqlConnection = DriverManager.getConnection(
                String.format("jdbc:mysql://%s/%s?", mysqlConfiguration.hostname, mysqlConfiguration.database),
                mysqlConfiguration.credentials.username, mysqlConfiguration.credentials.password);
    }

    @Override
    public void stop() throws Exception {
        Preconditions.checkState(mysqlConnection != null, "The context has not been started.");
        mysqlConnection.close();
        mysqlConnection = null;
    }

    public Connection getMysqlConnection() {
        Preconditions.checkState(mysqlConnection != null,
                "The context must be started to acquire a database connection.");
        return mysqlConnection;
    }


    private static void initializeCustomConverters(){
        CustomConverterFactory converterFactory = new CustomConverterFactory();

        TypeConverter bc = converterFactory.getBooleanConverter();
        TypeConverter dc = converterFactory.getDoubleConverter();
        TypeConverter lc = converterFactory.getLongConverter();
        TypeConverter ic = converterFactory.getIntegerConverter();
        TypeConverter dtc = converterFactory.getDateConverter();
        TypeConverter uc = converterFactory.getUUIDConverter();
        TypeConverter ttc = converterFactory.getCategoryTypesConverter();
        TypeConverter ctc = converterFactory.getTransactionTypesConverter();

        TypeConverter$.MODULE$.registerConverter(bc);
        TypeConverter$.MODULE$.registerConverter(dc);
        TypeConverter$.MODULE$.registerConverter(ic);
        TypeConverter$.MODULE$.registerConverter(lc);
        TypeConverter$.MODULE$.registerConverter(dtc);
        TypeConverter$.MODULE$.registerConverter(uc);
        TypeConverter$.MODULE$.registerConverter(ttc);
        TypeConverter$.MODULE$.registerConverter(ctc);
    }

}
