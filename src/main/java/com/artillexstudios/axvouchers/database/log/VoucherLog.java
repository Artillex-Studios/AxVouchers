package com.artillexstudios.axvouchers.database.log;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

public class VoucherLog {
    private final UUID uuid;
    private final String name;
    private final ArrayList<Entry> entries = new ArrayList<>();

    public VoucherLog(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public void add(Entry entry) {
        this.entries.add(entry);
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public record Entry(int id, String type, Timestamp time, UUID uuid, String duped) {
    }
}
