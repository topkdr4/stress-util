package ru.vetoshkin.stress.storage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.vetoshkin.stress.Response;
import ru.vetoshkin.stress.consumer.StatConsumer;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;





public class Storage {
    private static final Path   ROOT_PATH = Paths.get(System.getProperty("user.home"), "stress-data");
    private static final String PREFIX = "jdbc:sqlite:";
    private static final String SQL_TABLE_QUERY;
    private static final String INSERT_ONE = "INSERT INTO t_statistics(start_time, end_time, diff_time, success, error, http_code) VALUES(?, ?, ?, ?, ?, ?)";
    private static final String STAT_QUERY;

    static {
        SQL_TABLE_QUERY = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS t_statistics (").append('\n')
                .append("id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,").append('\n')
                .append("start_time NUMERIC NOT NULL,").append('\n')
                .append("end_time   NUMERIC NOT NULL,").append('\n')
                .append("diff_time  NUMERIC NOT NULL,").append('\n')
                .append("success    BOOLEAN NOT NULL,").append('\n')
                .append("error      BOOLEAN NOT NULL,").append('\n')
                .append("http_code  INTEGER NOT NULL").append('\n')
                .append(");")
                .append("\n\n\n")
                .append("PRAGMA synchronous=OFF;")
                .append("\n\n\n")
                .append("PRAGMA cache_size=-4096;")
                .append("\n\n\n")
                .append("PRAGMA journal_mode=MEMORY;")
                .toString()
        ;


        STAT_QUERY = "select\n"
                + "    count(*) count,\n"
                + "    (select count(*) from t_statistics where success = 1) success,\n"
                + "    (select max(a.diff_time) from (\n"
                + "        select diff_time\n"
                + "        from t_statistics\n"
                + "        where success = 1\n"
                + "        order by diff_time\n"
                + "        limit (select 80 * ((select (count(*) + 1) / 100 from t_statistics where success = 1)))) A    \n"
                + "    ) p80,\n"
                + "    (select max(a.diff_time) from (\n"
                + "        select diff_time\n"
                + "        from t_statistics\n"
                + "        where success = 1\n"
                + "        order by diff_time\n"
                + "        limit (select 90 * ((select (count(*) + 1) / 100 from t_statistics where success = 1)))) A    \n"
                + "    ) p90,\n"
                + "    (select max(a.diff_time) from (\n"
                + "        select diff_time\n"
                + "        from t_statistics\n"
                + "        where success = 1\n"
                + "        order by diff_time\n"
                + "        limit (select 99 * ((select (count(*) + 1) / 100 from t_statistics where success = 1)))) A    \n"
                + "    ) p99\n"
                + "from t_statistics";
    }

    private final String dbFile;
    private final ReentrantLock lock = new ReentrantLock();
    private DataSource dataSource;


    public Storage() {
        this("stress_" + System.currentTimeMillis() + ".db");
    }



    public Storage(String fileName) {
        try {
            Path path = ROOT_PATH.resolve(fileName);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            this.dbFile = PREFIX + path.toString();

            config();
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void insertResponses(List<Response> responses) throws SQLException {
        try {
            lock.lock();
            __insertResponses(responses);
        } finally {
            lock.unlock();
        }

    }


    private void __insertResponses(List<Response> responses) throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbFile);
                PreparedStatement statement = connection.prepareStatement(INSERT_ONE)) {

            connection.setAutoCommit(false);

            for (Response response : responses) {
                long start = response.getStart();
                long end   = response.getEnd();
                long diff  = end - start;

                statement.setLong(1, start);
                statement.setLong(2, end);
                statement.setLong(3, diff);
                statement.setBoolean(4, response.isSuccess());
                statement.setBoolean(5, response.isTransportError());
                statement.setInt(6, response.getHttpStatusCode());

                statement.addBatch();
            }

            statement.executeBatch();

            connection.commit();
        }
    }


    public StatConsumer.StatData getStat() throws SQLException {
        try {
            lock.lock();

            return __getStat();
        } finally {
            lock.unlock();
        }
    }


    private StatConsumer.StatData __getStat() throws SQLException {
        StatConsumer.StatData result = new StatConsumer.StatData();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(STAT_QUERY);

            if (resultSet.next()) {
                result.count = resultSet.getLong("count");
                result.successCount = resultSet.getLong("success");
                result.p80 = resultSet.getLong("p80");
                result.p90 = resultSet.getLong("p90");
                result.p99 = resultSet.getLong("p99");
            }

        }

        return result;
    }


    private void init() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(SQL_TABLE_QUERY);
        }
    }


    private void config() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(1);
        config.setJdbcUrl(dbFile);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");


        this.dataSource = new HikariDataSource(config);
    }

}
