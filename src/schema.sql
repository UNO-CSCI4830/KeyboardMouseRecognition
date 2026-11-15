CREATE TABLE Sequence (
    SequenceID INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE Action (
    ActionID INTEGER PRIMARY KEY AUTOINCREMENT,
    SequenceID INTEGER NOT NULL,
    ActionOrder INTEGER NOT NULL,
    ActionType TEXT NOT NULL,
    ActionValue TEXT NOT NULL,
    KeyCode INTEGER,
    DelayMs INTEGER,
    WindowInfo TEXT,
    FOREIGN KEY (SequenceID) REFERENCES Sequence(SequenceID)
    ON DELETE CASCADE
);

CREATE TABLE SequenceStats (
    SequenceID INTEGER PRIMARY KEY,
    DetectionCount INTEGER NOT NULL DEFAULT 0,
    ApprovalCount INTEGER NOT NULL DEFAULT 0,
    DismissalCount INTEGER NOT NULL DEFAULT 0,
    ReplaySpeed REAL NOT NULL DEFAULT 1.5,
    LastSuggested DATETIME,
    LastApproved DATETIME,
    FOREIGN KEY (SequenceID) REFERENCES Sequence(SequenceID)
    ON DELETE CASCADE
);
