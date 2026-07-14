package com.artillexstudios.axvouchers.database.log;

import java.sql.Timestamp;

public record VoucherLogEntry(int id, String type, Timestamp time, Object uuid, String duped, String placeholders) {

}
