/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.ChatColor;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.util.Objects;

@SuppressWarnings("deprecation")
public class CommandFeedback {

	/**
	 * Create private constructor to hide implicit one
	 */
	private CommandFeedback() {

	}

	/**
	 * A helper class which helps to reduce boilerplate player.sendMessage code by
	 * providing the strings to send instead
	 * of having to copy and paste them.
	 *
	 * @param feedback The string to send back
	 * @param success  An optional Boolean which is used for the success feedback.
	 *                 If none is passed, success just
	 *                 replies "Success!"
	 * @return The Feedback string
	 */
	@SuppressWarnings("java:S1192")
	public static String sendCommandFeedback(String feedback, Boolean... success) {
		String feedbackMessage;
		String chatPrefix = getChatPrefix();
		YamlDocument languageFile = HoneypotConfigManager.getLanguageFile();

		switch (feedback.toLowerCase()) {
			case "usage" -> feedbackMessage = ("\n \n \n \n \n \n-----------------------\n \n" + chatPrefix + " "
					+ ChatColor.WHITE + "Need Help?\n" + "  " + "/honeypot " + ChatColor.GRAY + "create [block]\n"
					+ "  "
					+ ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "remove (all | near) (optional)\n" + "  "
					+ ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "reload\n" + "  " + ChatColor.WHITE
					+ "/honeypot "
					+ ChatColor.GRAY + "locate\n" + "  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "gui\n"
					+ "  "
					+ ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "history [query | delete | purge] \n \n"
					+ ChatColor.WHITE + "-----------------------");

			case "kick" -> feedbackMessage = chatPrefix + " " + ChatColor.translateAlternateColorCodes('&',
					Objects.requireNonNull(languageFile.getString("kick-reason"), "Kick reason is null"));

			case "ban" -> feedbackMessage = chatPrefix + " " + ChatColor.translateAlternateColorCodes('&',
					Objects.requireNonNull(languageFile.getString("ban-reason"), "Ban reason is null"));

			case "warn" -> feedbackMessage = chatPrefix + " " + ChatColor.translateAlternateColorCodes('&',
					Objects.requireNonNull(languageFile.getString("warn-message"), "Warn message is null"));

			case "alreadyexists" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("already-exists")));

			case "success" -> {
				if (success.length > 0 && success[0].equals(true)) {
					feedbackMessage = (chatPrefix + " "
							+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("success.created")));

				} else if (success.length > 0 && success[0].equals(false)) {
					feedbackMessage = (chatPrefix + " "
							+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("success.removed")));

				} else {
					feedbackMessage = (chatPrefix + " "
							+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("success.default")));

				}
			}

			case "notapot" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("not-a-honeypot")));

			case "nopermission" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-permission")));

			case "reload" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("reload")));

			case "foundpot" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("found-pots")));

			case "nopotfound" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-pots-found")));

			case "updateavailable" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("update-available")));

			case "againstfilter" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("against-filter")));

			case "notlookingatblock" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("not-looking-at-block")));

			case "noexist" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-exist")));

			case "deletedall" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("deleted.all")));

			case "deletednear" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("deleted.near")));

			case "worldguard" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("worldguard")));

			case "griefprevention" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("griefprevention")));

			case "staffbroke" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("staff-broke")));

			case "exemptnobreak" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("exempt-no-break")));

			case "searching" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("searching")));

			case "truncating" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("truncating")));

			case "notonline" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("not-online")));

			case "nohistory" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-history")));

			case "lands" -> feedbackMessage = (chatPrefix + " "
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("lands")));

			case "debug" -> {
				if (success.length > 0 && success[0].equals(true)) {
					feedbackMessage = (chatPrefix + " "
							+ "Debug mode has been enabled. Right click any block to check its PDC");

				} else if (success.length > 0 && success[0].equals(false)) {
					feedbackMessage = (chatPrefix + " "
							+ "Debug mode has been disabled");

				} else {
					feedbackMessage = (chatPrefix + " "
							+ "Debug mode is only useful while using PDC");

				}
			}
			default -> feedbackMessage = (chatPrefix + " " + ChatColor.DARK_RED
					+ ChatColor.translateAlternateColorCodes('&', languageFile.getString("unknown-error")));
		}
		return feedbackMessage;
	}

	/**
	 * Return the chat prefix object from config
	 *
	 * @return The chat prefix, preformatted with color and other modifiers
	 */
	public static String getChatPrefix() {
		return ChatColor.translateAlternateColorCodes('&',
				Objects.requireNonNull(HoneypotConfigManager.getLanguageFile().getString("prefix")));
	}

}
