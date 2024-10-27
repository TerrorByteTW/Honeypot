/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright TerrorByte & Honeypot Contributors (c) 2022 - 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storagemanager.queue;

import java.util.*;

public class ListenableQueue<E> extends AbstractQueue<E> {
    private static final Object lock = new Object();
    private final Queue<E> delegate;
    private final List<Listener<E>> listeners = new ArrayList<>();

    public ListenableQueue(Queue<E> delegate) {
        this.delegate = delegate;
    }

    public ListenableQueue<E> registerListener(Listener<E> listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public boolean offer(E e) {
        if (delegate.offer(e)) {
            synchronized (lock) {
                lock.notify();
                listeners.forEach(listener -> listener.onElementAdded(e));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public E poll() {
        return delegate.poll();
    }

    @Override
    public E peek() {
        return delegate.peek();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    interface Listener<E> {
        void onElementAdded(E element);
    }

}
