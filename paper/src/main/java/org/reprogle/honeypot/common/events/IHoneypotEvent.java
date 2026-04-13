package org.reprogle.honeypot.common.events;

public interface IHoneypotEvent {
    default boolean isOptional() {
        return false;
    }
}
