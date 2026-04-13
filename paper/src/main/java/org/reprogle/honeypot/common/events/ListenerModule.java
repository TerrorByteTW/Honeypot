package org.reprogle.honeypot.common.events;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ListenerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<IHoneypotEvent> eventBinder = Multibinder.newSetBinder(binder(), IHoneypotEvent.class);
        eventBinder.addBinding().to(BlockBreakEventListener.class);
        eventBinder.addBinding().to(BlockBurnEventListener.class);
        eventBinder.addBinding().to(BlockFormEventListener.class);
        eventBinder.addBinding().to(BlockFromToEventListener.class);
        eventBinder.addBinding().to(EntityChangeBlockEventListener.class);
        eventBinder.addBinding().to(EntityExplodeEventListener.class);
        eventBinder.addBinding().to(InventoryClickDragEventListener.class);
        eventBinder.addBinding().to(InventoryMoveItemEventListener.class);
        eventBinder.addBinding().to(LeavesDecayEventListener.class);
        eventBinder.addBinding().to(PistonExtendRetractListener.class);
        eventBinder.addBinding().to(PlayerInteractEventListener.class);
        eventBinder.addBinding().to(PlayerJoinEventListener.class);
        eventBinder.addBinding().to(SignChangeEventListener.class);
        eventBinder.addBinding().to(StructureGrowEventListener.class);
    }
}
