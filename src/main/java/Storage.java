import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;





public class Storage {
    private static final String DB_PATH = System.getProperty("user.home") + "/stress-data/";
    private static final String PREFIX = "jdbc:sqlite:";
    private final String dbFile;



    public Storage(String fileName) throws Exception {
        this.dbFile = PREFIX + DB_PATH + fileName;

        Path path = Paths.get(DB_PATH);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        init();
    }


    public static void main(String[] args) throws Exception {
        new Storage("stress_" + System.currentTimeMillis() + ".db");
    }



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

    private void init() throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbFile)) {
            Statement statement = connection.createStatement();
            statement.execute(SQL_TABLE_QUERY);
        }
    }
}
