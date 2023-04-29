package org.reprogle.honeypot.providers.exceptions;

public class BehaviorAlreadyRegisteredException extends Exception{
    public BehaviorAlreadyRegisteredException(String errorMessage) {
        super(errorMessage);
    }
}
