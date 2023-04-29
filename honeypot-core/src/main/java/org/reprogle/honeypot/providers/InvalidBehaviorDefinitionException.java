package org.reprogle.honeypot.providers;

public class InvalidBehaviorDefinitionException extends Exception{
    public InvalidBehaviorDefinitionException(String errorMessage) {
        super(errorMessage);
    }
}
