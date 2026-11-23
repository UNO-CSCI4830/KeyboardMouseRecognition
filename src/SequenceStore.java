import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequenceStore {

    private final Connection connection;
    private final List<Sequence> sequences = new ArrayList<>();

    public SequenceStore(String databaseFile) {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema();
            loadSequencesFromDatabase(); 

        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to SQLite database.",e);
        }
    }

    private void initializeSchema() {

        try {
            // Update this when we change file structure. 
            String schema = java.nio.file.Files.readString(java.nio.file.Path.of("src", "schema.sql"));

            try (Statement stmt = connection.createStatement()) {

                for (String statement : schema.split(";")) {

                    String sql = statement.trim();

                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema.",e);
        }
    }

    private void loadSequencesFromDatabase() {

        sequences.clear();

        String sql =
                """
                SELECT SequenceID
                FROM Sequence
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                int sequenceId = rs.getInt("SequenceID");
                sequences.add(getSequence(sequenceId));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sequences.",e);
        }
    }

    public void saveSequence(Sequence sequence) {

        String sequenceSql = "INSERT INTO Sequence DEFAULT VALUES";
        String actionSql =
            """
            INSERT INTO Action
            (
                SequenceID,
                ActionOrder,
                ActionType,
                ActionValue,
                KeyCode,
                DelayMs,
                WindowInfo
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (
            PreparedStatement sequenceStmt = connection.prepareStatement(sequenceSql, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement actionStmt = connection.prepareStatement(actionSql)
        ) {

            connection.setAutoCommit(false);

            sequenceStmt.executeUpdate();

            try (ResultSet generatedKeys = sequenceStmt.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException("Failed to create sequence.");
                }

                int sequenceId = generatedKeys.getInt(1);
                sequence.setId(sequenceId);

                String statsSql =
                    """
                    INSERT INTO SequenceStats
                    (
                        SequenceID
                    )
                    VALUES (?)
                    """;

                try (PreparedStatement statsStmt =
                        connection.prepareStatement(statsSql)) {

                    statsStmt.setInt(1, sequenceId);
                    statsStmt.executeUpdate();
                }

                List<Action> actions = sequence.getActions();
                for (int i = 0; i < actions.size(); i++) {

                    Action action = actions.get(i);
                    actionStmt.setInt(1, sequenceId);
                    actionStmt.setInt(2, i);
                    actionStmt.setString(3, action.getType().name());
                    actionStmt.setString(4, action.getValue());

                    if (action.hasKeyCode()) {
                        actionStmt.setInt(5, action.getKeyCode());
                    } else {
                        actionStmt.setNull(5, Types.INTEGER);
                    }

                    actionStmt.setInt(6, action.getDelayMs());
                    actionStmt.setString(7, action.getWindowInfo());
                    actionStmt.addBatch();
                }
                actionStmt.executeBatch();
            }
            connection.commit();
            sequences.add(sequence); 

        } catch (SQLException e) {

            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Failed to save sequence.", e);
        // Ensures the autoCommit is set back to the state it was.
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public Sequence getSequence(int id) {
        Sequence sequence = new Sequence();
        sequence.setId(id);

        String sql =
                """
                SELECT *
                FROM Action
                WHERE SequenceID = ?
                ORDER BY ActionOrder
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Action action = new Action(
                        ActionType.valueOf(rs.getString("ActionType")),
                        rs.getString("ActionValue"),
                        rs.getInt("KeyCode"),
                        rs.getInt("DelayMs"),
                        rs.getString("WindowInfo"));

                    sequence.addAction(action);
                }
            }
            return sequence;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve sequence.", e);
        }
    }

    public List<Sequence> getSequences() {
        return Collections.unmodifiableList(sequences);
    }

    public void deleteSequence(int id) {

        String sql =
                """
                DELETE FROM Sequence
                WHERE SequenceID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            sequences.removeIf(s -> s.getId() == id);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete sequence.", e);
        }
    }

    public SequenceStats getSequenceStats(int id) {

        String sql =
                """
                SELECT *
                FROM SequenceStats
                WHERE SequenceID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()){

                if (!rs.next()) {
                    return null;
                }
            
                return new SequenceStats(
                    rs.getInt("SequenceID"),
                    rs.getInt("DetectionCount"),
                    rs.getInt("ApprovalCount"),
                    rs.getInt("DismissalCount"),
                    rs.getDouble("ReplaySpeed"),
                    rs.getTimestamp("LastSuggested"),
                    rs.getTimestamp("LastApproved")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve sequence stats.", e);
        }
    }

    public void recordApproval(int id) {

        String sql =
                """
                UPDATE SequenceStats
                SET ApprovalCount = ApprovalCount + 1,
                    LastApproved = CURRENT_TIMESTAMP
                WHERE SequenceID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to record approval.", e);
        }
    }

    public void recordDismissal(int id) {

        String sql =
                """
                UPDATE SequenceStats
                SET DismissalCount = DismissalCount + 1
                WHERE SequenceID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to record dismissal.",e);
        }
    }

    public void recordDetection(int id) {

        String sql =
                """
                UPDATE SequenceStats
                SET DetectionCount = DetectionCount + 1,
                    LastSuggested = CURRENT_TIMESTAMP
                WHERE SequenceID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to record detection.",e);
        }
    }

    public void updateReplaySpeed(int id, double replaySpeed) {

        String sql =
                """
                UPDATE SequenceStats
                SET ReplaySpeed = ?
                WHERE SequenceID = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setDouble(1, replaySpeed);
            stmt.setInt(2, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update replay speed.",e);
        }
    }

    public void close() {

        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection.", e);
        }
    }
}