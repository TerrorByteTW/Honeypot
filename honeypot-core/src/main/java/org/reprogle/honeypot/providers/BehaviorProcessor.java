package org.reprogle.honeypot.providers;

import java.util.ArrayList;

public class BehaviorProcessor {

    private final ArrayList<BehaviorProvider> behaviorProviders = new ArrayList<BehaviorProvider>();

    public void registerBehavior(BehaviorProvider behavior) throws InvalidBehaviorDefinitionException {
        if (!behavior.getClass().isAnnotationPresent(Behavior.class))
            throw new InvalidBehaviorDefinitionException("Behavior " + behavior.getClass().getName() + " is missing the @Behavior annotation. This is *not* an issue with Honeypot, but rather the plugin that attempted to register this behavior provider! Do not report this to the author");

        behaviorProviders.add(behavior);
    }

    public BehaviorProvider getBehaviorProvider(String name) {
        for (BehaviorProvider behavior : behaviorProviders) {
            if (behavior.getProviderName().equals(name))
                return behavior;
        }

        return null;
    }

}
