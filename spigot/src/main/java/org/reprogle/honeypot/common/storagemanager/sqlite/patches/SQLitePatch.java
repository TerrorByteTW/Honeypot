package org.reprogle.honeypot.common.storagemanager.sqlite.patches;

import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.sql.SQLException;
import java.sql.Statement;

public interface SQLitePatch {

    void update(Statement s, HoneypotLogger logger) throws SQLException;
}
