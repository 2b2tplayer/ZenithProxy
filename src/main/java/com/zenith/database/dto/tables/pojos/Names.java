/*
 * This file is generated by jOOQ.
 */
package com.zenith.database.dto.tables.pojos;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Names implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final UUID uuid;
    private final OffsetDateTime changedtoat;
    private final OffsetDateTime changedfromat;

    public Names(Names value) {
        this.name = value.name;
        this.uuid = value.uuid;
        this.changedtoat = value.changedtoat;
        this.changedfromat = value.changedfromat;
    }

    public Names(
            String name,
            UUID uuid,
            OffsetDateTime changedtoat,
            OffsetDateTime changedfromat
    ) {
        this.name = name;
        this.uuid = uuid;
        this.changedtoat = changedtoat;
        this.changedfromat = changedfromat;
    }

    /**
     * Getter for <code>public.names.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for <code>public.names.uuid</code>.
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Getter for <code>public.names.changedtoat</code>.
     */
    public OffsetDateTime getChangedtoat() {
        return this.changedtoat;
    }

    /**
     * Getter for <code>public.names.changedfromat</code>.
     */
    public OffsetDateTime getChangedfromat() {
        return this.changedfromat;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Names (");

        sb.append(name);
        sb.append(", ").append(uuid);
        sb.append(", ").append(changedtoat);
        sb.append(", ").append(changedfromat);

        sb.append(")");
        return sb.toString();
    }
}
