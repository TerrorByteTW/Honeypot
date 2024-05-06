package org.reprogle.honeypot.common.storagemanager.sqlite.patches;

import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.sql.SQLException;
import java.sql.Statement;

// This is titled 00 since, if we ever need any further patches to the database, we can create new files for them.
@SuppressWarnings("FieldCanBeLocal")
public class UpdatePlayerHistoryTable00 implements SQLitePatch {

    private final String UPDATE_PLAYER_TABLE_00 = "ALTER TABLE honeypot_players ADD `type` " +
            "VARCHAR NOT NULL default 'prelimBreak';";

    public void update(Statement statement, HoneypotLogger logger) throws SQLException {
        logger.debug("Attempting to apply SQLite patch UpdatePlayerTable00");

        try {
            statement.executeUpdate(UPDATE_PLAYER_TABLE_00);
        } catch (SQLException e) {
            logger.debug("Altering table honeypot_players to add `type` column failed. " +
                    "This is likely because the plugin is being run for the very first time, " +
                    "and the pragma was set at 0 but none of the tables existed yet");
            logger.debug("This is not really a concern");
        }

        logger.debug("Completed patch UpdatePlayerTable00!");
    }
}
