package ru.vetoshkin.stress.storage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.vetoshkin.stress.Response;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;





public class Storage {
    private static final Path   ROOT_PATH = Paths.get(System.getProperty("user.home"), "stress-data");
    private static final String PREFIX = "jdbc:sqlite:";
    private static final String SQL_TABLE_QUERY;
    private static final String INSERT_ONE = "INSERT INTO t_statistics(start_time, end_time, diff_time, success, error, http_code) VALUES(?, ?, ?, ?, ?, ?)";

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
                .toString()
        ;
    }

    private final String dbFile;
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
        try (Connection connection = DriverManager.getConnection(dbFile);
             PreparedStatement statement = connection.prepareStatement(INSERT_ONE)) {

            connection.setAutoCommit(true);

            for (Response response : responses) {
                long start = response.getStart();
                long end   = response.getEnd();
                long diff  = end - start;

                statement.setLong(1, start);
                statement.setLong(2, end);
                statement.setLong(3, diff);
                statement.setBoolean(4, response.isSuccess());
                statement.setBoolean(5, response.isError());
                statement.setInt(6, response.getHttpStatusCode());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }


    private void init() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(SQL_TABLE_QUERY);
        }
    }


    private void config() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setJdbcUrl(dbFile);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");


        this.dataSource = new HikariDataSource(config);
    }

}
