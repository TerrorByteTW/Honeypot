/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
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
        eventBinder.addBinding().to(BlockPlaceEventListener.class);
        eventBinder.addBinding().to(HoneypotWandListeners.class);
    }
}
