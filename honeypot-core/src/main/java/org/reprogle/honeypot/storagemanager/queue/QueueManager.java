package org.reprogle.honeypot.storagemanager.queue;

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

    ListenableQueue<PreparedStatement> queue = new ListenableQueue<>(new LinkedList<>());
    private static QueueManager instance = null;

    private QueueManager() {
        queue.registerListener(element -> {
            // If an SQL query is added to the queue but the queue has more than one lined
            // up, we need to go ahead and handle them all
            while (queue.peek() != null) {
                PreparedStatement ps = queue.poll();
                try {
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
