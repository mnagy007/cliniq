package com.cliniq.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all aggregate roots.
 *
 * <p>Manages a transient list of {@link DomainEvent}s raised during a single
 * command execution. The application service drains this list via
 * {@link #pullDomainEvents()} immediately after persisting the aggregate,
 * then writes each event to the outbox — all within the same transaction.
 *
 * <p>{@code pullDomainEvents()} clears the internal list, so calling it a
 * second time within the same unit of work returns an empty list. This is
 * intentional: events are owned by the transaction that produced them.
 *
 * @param <ID> the strongly-typed aggregate identifier
 */
public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Records a domain event to be published after the aggregate is persisted.
     * Called only from within aggregate methods — never from outside.
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Returns all events raised since the last drain and clears the internal list.
     * The application service must call this exactly once per command, after saving.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> snapshot = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return snapshot;
    }

    /** Returns the aggregate's strongly-typed identifier. */
    public abstract ID getId();
}
