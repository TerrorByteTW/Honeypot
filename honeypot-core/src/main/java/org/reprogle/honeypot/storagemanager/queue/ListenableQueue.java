package org.reprogle.honeypot.storagemanager.queue;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class ListenableQueue<E> extends AbstractQueue<E> {
    interface Listener<E> {
        void onElementAdded(E element);
    }

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
            listeners.forEach(listener -> listener.onElementAdded(e));
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

}
