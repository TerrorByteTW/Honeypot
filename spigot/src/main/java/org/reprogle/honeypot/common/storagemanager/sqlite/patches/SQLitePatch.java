package org.reprogle.honeypot.common.storagemanager.sqlite.patches;

import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.sql.SQLException;
import java.sql.Statement;

public interface SQLitePatch {

    /**
     * The patch to apply
     * @param s The statement of the connection
     * @param logger The logger to log any potential errors
     * @throws SQLException Thrown if an error occurs
     */
    void update(Statement s, HoneypotLogger logger) throws SQLException;

    /**
     * The user_version pragma that the database patch applies to. This allows us to ignore unnecessary patches
     * @return user_version of patch
     */
    int patchedIn();
}
