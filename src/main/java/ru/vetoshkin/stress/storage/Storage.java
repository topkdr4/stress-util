package ru.vetoshkin.stress.storage;
import ru.vetoshkin.stress.Response;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.StringJoiner;





public class Storage {
    private static final String DB_PATH = System.getProperty("user.home") + "/stress-data/";
    private static final String PREFIX = "jdbc:sqlite:";
    private static final String SQL_TABLE_QUERY;

    static {
        SQL_TABLE_QUERY = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS statistics (").append('\n')
                .append("start_time INTEGER NOT NULL,").append('\n')
                .append("end_time   INTEGER NOT NULL,").append('\n')
                .append("diff_time  INTEGER NOT NULL").append('\n')
                .append(");")
                .toString()
        ;
    }

    private final String dbFile;



    public Storage(String fileName) throws Exception {
        this.dbFile = PREFIX + DB_PATH + fileName;

        Path path = Paths.get(DB_PATH);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        init();
    }



    private static final String INSERT_ONE = "INSERT INTO statistics(start_time, end_time, diff_time) VALUES(?, ?, ?)";


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

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }



    private void init() throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbFile);
             Statement statement = connection.createStatement()) {

            statement.execute(SQL_TABLE_QUERY);
        }
    }
}
