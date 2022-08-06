package org.reprogle.honeypot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;

public class HoneypotLogger {

	private File logFile;

	/**
	 * Initialize the Honeypot logger and create it if it doesn't exist
	 */
	public HoneypotLogger() {
		try {
			logFile = new File(Honeypot.getPlugin().getDataFolder(), "logs.txt");
			if (logFile.createNewFile()) {
				Honeypot.getPlugin().getLogger().info("Logs file created: " + logFile.getName());
			}
		} catch (IOException e) {
			Honeypot.getPlugin().getLogger().severe("Could not create the logs.txt file for logging!");
		}
	}

	/**
	 * Log a message to the log file. Automatically prepends date and time
	 * @param message The message to log
	 */
	public void log(String message) {
		if(Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-logging"))) return;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))){
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();
            bw.append("[" + dtf.format(now) + "] " + message + "\n");
        } catch (IOException e) {
            Honeypot.getPlugin().getLogger().warning("An error occured while attempting to log to the logs.txt file! " + e);
        }
	}

}
