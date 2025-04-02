/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storagemanager.queue;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * A class to manage the PreparedStatement queues.
 * Utilizes a custom queue, see {@link ListenableQueue}
 *
 * @see ListenableQueue
 */
public class QueueManager {

    private static QueueManager instance = null;
    final ListenableQueue<PreparedStatement> queue = new ListenableQueue<>(new LinkedList<>());

    private QueueManager() {
        queue.registerListener(element -> {
            // If an SQL query is added to the queue but the queue has more than one lined
            // up, we need to go ahead and handle them all
            while (queue.peek() != null) {
                try (PreparedStatement ps = queue.poll()) {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Returns the QueueManager instance
     *
     * @return {@link QueueManager}
     */
    public static synchronized QueueManager getInstance() {
        if (instance == null)
            instance = new QueueManager();

        return instance;
    }

    /**
     * A simple function that adds a PreparedStatement to the queue.
     *
     * @param ps A prepared statement to add to the queue
     * @return True if successfully added, false if not
     */
    public boolean addToQueue(PreparedStatement ps) {
        return queue.offer(ps);
    }

}
