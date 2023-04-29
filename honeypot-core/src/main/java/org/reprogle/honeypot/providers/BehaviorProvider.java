package org.reprogle.honeypot.providers;

import com.google.common.base.Objects;
import org.bukkit.entity.Player;

public abstract class BehaviorProvider {

    protected final String providerName;
    protected final BehaviorTypes behaviorType;

    BehaviorProvider(String providerName) {
        this.providerName = providerName;
        this.behaviorType = getClass().getAnnotation(Behavior.class).type();
    }

    /**
     * This should return the name of the behavior provider
     * @return The name of the behavior provider
     */
    public String getProviderName(){
        return providerName;
    }

    public BehaviorTypes getBehaviorType() {
        return behaviorType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BehaviorProvider behavior)) return false;
        if (o == this) return true;

        // Don't really care about the type since providerName must be unique
        return behavior.getProviderName().equals(this.providerName);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(providerName, behaviorType);
    }

    /**
     * A method to be executed when an action requires processing.
     * This is ignored if the <code>BehaviorType</code> is not set to <code>BehaviorTypes.CUSTOM</code>
     * @param p The {@link org.bukkit.entity.Player} who the behavior provider will process against
     * @return Your behavior provider should return true if the processing is successful, otherwise return false.
     */
    public abstract boolean process(Player p);
}
