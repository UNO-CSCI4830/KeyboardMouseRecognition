import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SequenceStore {

    private final Connection connection;

    public SequenceStore(String databaseFile) {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema();

        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to SQLite database.",e);
        }
    }

    private void initializeSchema() {

        try {

            String schema = java.nio.file.Files.readString(java.nio.file.Path.of("schema.sql"));

            try (Statement stmt = connection.createStatement()) {
                // Test and make sure this creates all of the tables
                stmt.execute(schema);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema.",e);
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

    public void saveSequenceStats(SequenceStats stats) {

        String sql =
                """
                INSERT INTO SequenceStats
                (
                    SequenceID,
                    DetectionCount,
                    ApprovalCount,
                    DismissalCount,
                    ReplaySpeed,
                    LastSuggested,
                    LastApproved
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, stats.getSequenceId());
            stmt.setInt(2, stats.getDetectionCount());
            stmt.setInt(3, stats.getApprovalCount());
            stmt.setInt(4, stats.getDismissalCount());
            stmt.setDouble(5, stats.getReplaySpeed());
            stmt.setTimestamp(6, stats.getLastSuggested());
            stmt.setTimestamp(7, stats.getLastApproved());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save sequence stats.", e);
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

        List<Sequence> sequences = new ArrayList<>();

        String sql =
                """
                SELECT SequenceID
                FROM Sequence
                """;

        try (
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {
                sequences.add(getSequence(rs.getInt("SequenceID")));
            }

            return sequences;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve sequences.", e);
        }
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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