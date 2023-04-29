package org.reprogle.honeypot.providers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.providers.exceptions.BehaviorAlreadyRegisteredException;
import org.reprogle.honeypot.providers.exceptions.InvalidBehaviorDefinitionException;

import java.util.ArrayList;

public class BehaviorProcessor {

    private final ArrayList<BehaviorProvider> behaviorProviders = new ArrayList<>();

    /**
     * Register a behavior provider with Honeypot
     *
     * @param behavior The {@link BehaviorProvider} that should be registered
     * @throws InvalidBehaviorDefinitionException If your behavior provider isn't annotated properly,
     *         an InvalidBehaviorDefinitionException will be thrown
     */
    public boolean registerBehavior(@NotNull BehaviorProvider behavior) throws InvalidBehaviorDefinitionException, BehaviorAlreadyRegisteredException {
        if (!behavior.getClass().isAnnotationPresent(Behavior.class))
            throw new InvalidBehaviorDefinitionException("Behavior " + behavior.getClass().getName() + " is missing the @Behavior annotation. This is *not* an issue with Honeypot, but rather the plugin that attempted to register this behavior provider! Do not report this to the author");

        if (behaviorProviders.contains(behavior)) {
            throw new BehaviorAlreadyRegisteredException("Behavior " + behavior.getClass().getName() + " is already registered. This is *not* an issue with Honeypot, but rather the plugin that attempted to register this behavior provider! Do not report this to the author");
        }

        return behaviorProviders.add(behavior);
    }

    /**
     * Returns a behavior provider based on registered name
     *
     * @param name The name of the provider to pull
     * @return {@link BehaviorProvider} The behavior provider you requested
     */
    public BehaviorProvider getBehaviorProvider(String name) {
        for (BehaviorProvider behavior : behaviorProviders) {
            if (behavior.getProviderName().equals(name))
                return behavior;
        }

        return null;
    }

    /**
     * This method calls the correct processor function, depending on if the type of the behavior provider is <code>BehaviorTypes.CUSTOM</code> or not
     *
     * @param behavior The behavior provider to process
     * @param p The player to process against
     * @return True if successful, false if not
     */
    public boolean process(@NotNull BehaviorProvider behavior, Player p) {
        BehaviorTypes type = behavior.getClass().getAnnotation(Behavior.class).type();

        if (type.equals(BehaviorTypes.CUSTOM)) {
            return behavior.process(p);
        } else {
            return processInternal(behavior, p);
        }
    }

    /**
     * This method will handle built-in behaviors.
     *
     * @param behavior The behavior provider to process
     * @param p The player to process against
     * @return True if successful, false if not
     */
    public boolean processInternal(@NotNull BehaviorProvider behavior, Player p) {

        return true;
    }

}
