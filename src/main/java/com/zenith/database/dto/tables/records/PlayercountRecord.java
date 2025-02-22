/*
 * This file is generated by jOOQ.
 */
package com.zenith.database.dto.tables.records;


import com.zenith.database.dto.tables.Playercount;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;

import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PlayercountRecord extends TableRecordImpl<PlayercountRecord> implements Record2<OffsetDateTime, Short> {

    private static final long serialVersionUID = 1L;

    /**
     * Getter for <code>public.playercount.time</code>.
     */
    public OffsetDateTime getTime() {
        return (OffsetDateTime) get(0);
    }

    /**
     * Create a detached PlayercountRecord
     */
    public PlayercountRecord() {
        super(Playercount.PLAYERCOUNT);
    }

    /**
     * Setter for <code>public.playercount.time</code>.
     */
    public PlayercountRecord setTime(OffsetDateTime value) {
        set(0, value);
        return this;
    }

    /**
     * Create a detached, initialised PlayercountRecord
     */
    public PlayercountRecord(OffsetDateTime time, Short count) {
        super(Playercount.PLAYERCOUNT);

        setTime(time);
        setCount(count);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<OffsetDateTime, Short> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<OffsetDateTime, Short> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<OffsetDateTime> field1() {
        return Playercount.PLAYERCOUNT.TIME;
    }

    @Override
    public Field<Short> field2() {
        return Playercount.PLAYERCOUNT.COUNT;
    }

    @Override
    public OffsetDateTime component1() {
        return getTime();
    }

    @Override
    public Short component2() {
        return getCount();
    }

    @Override
    public OffsetDateTime value1() {
        return getTime();
    }

    @Override
    public Short value2() {
        return getCount();
    }

    @Override
    public PlayercountRecord value1(OffsetDateTime value) {
        setTime(value);
        return this;
    }

    @Override
    public PlayercountRecord value2(Short value) {
        setCount(value);
        return this;
    }

    @Override
    public PlayercountRecord values(OffsetDateTime value1, Short value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Getter for <code>public.playercount.count</code>.
     */
    public Short getCount() {
        return (Short) get(1);
    }

    /**
     * Setter for <code>public.playercount.count</code>.
     */
    public PlayercountRecord setCount(Short value) {
        set(1, value);
        return this;
    }
}
