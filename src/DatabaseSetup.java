import java.sql.*;
import java.util.Map;

public class DatabaseSetup {
    private Connection connection;

    public DatabaseSetup() throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        this.connection = DriverManager.getConnection("jdbc:mysql:///search_engine_ics?autoReconnect=true&useSSL=false",
                "mytestuser", "mypassword");
    }

    public void setUpDatabase(Map<String,PostingList> index, URLMapper urlMapper, Map<String,Double> distance) throws Exception {
        // createTables();
        populateTables(index, urlMapper, distance);
    }

    private void createTables() throws Exception {
        executeUpdate("DROP TABLE IF EXISTS scores");
        executeUpdate("DROP TABLE IF EXISTS documents");
        createScoresTable();
        createDocumentsTable();
    }

    private void createScoresTable() throws Exception {
        executeUpdate("CREATE TABLE scores ( " +
                "term VARCHAR(300), " +
                "docID VARCHAR(25), " +
                "score DOUBLE, " +
                "PRIMARY KEY (term, docID) )");
    }

    private void createDocumentsTable() throws Exception {
        executeUpdate("CREATE TABLE documents ( " +
                "docID VARCHAR(100), " +
                "url text, " +
                "distance DOUBLE, " +
                "PRIMARY KEY (docID) )");
    }

    private void populateTables(Map<String,PostingList> index, URLMapper urlMapper, Map<String,Double> distance) throws Exception {
        populateScoresTable(index);
        populateDocumentsTable(urlMapper, distance);
    }

    private void populateScoresTable(Map<String,PostingList> index) throws Exception {
        for (Map.Entry<String,PostingList> entry : index.entrySet()) {
            for (PostingDocument document : entry.getValue().getDocuments()) {
                executeUpdate(String.format(
                        "INSERT INTO scores VALUES(\"%s\", \"%s\", %s)",
                        entry.getKey(), document.getDocID(), document.getScore()));
            }
        }
    }

    private void populateDocumentsTable(URLMapper urlMapper, Map<String,Double> distance) throws Exception {
        ResultSet result = executeQuery("SELECT docID FROM scores GROUP BY docID");
        while (result.next()) {
            ResultSet present = executeQuery(String.format(
                    "SELECT * FROM documents WHERE docID = '%s'", result.getString(1)));
            if (!present.isBeforeFirst()) {
                executeUpdate(String.format(
                        "INSERT INTO documents VALUES(\"%s\", \"%s\", %s)",
                        result.getString(1),
                        urlMapper.getURL(result.getString(1)),
                        distance.get(result.getString(1))));
            }
        }
    }

    private int executeUpdate(String query) throws Exception {
        Statement update = this.connection.createStatement();
        return update.executeUpdate(query);
    }

    private ResultSet executeQuery(String query) throws Exception {
        Statement select = this.connection.createStatement();
        return select.executeQuery(query);
    }

}
