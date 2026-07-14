package com.artillexstudios.axvouchers.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvouchers.database.log.VoucherLogEntry;
import com.artillexstudios.axvouchers.voucher.Voucher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DataHandler {
    private final DatabaseHandler handler;
    private final DatabaseQuery<?> antidupeInsertQuery;
    private final DatabaseQuery<?> incrementUsedQuery;
    private final DatabaseQuery<?> inserLogQuery;
    private final DatabaseQuery<List<VoucherLogEntry>> selectLogsQuery;
    private final DatabaseQuery<User> userSelectQuery;
    private final DatabaseQuery<Integer> userInsertQuery;

    public DataHandler(DatabaseHandler handler) {
        this.handler = handler;
        this.antidupeInsertQuery = this.handler.query("antidupe_insert");
        this.incrementUsedQuery = this.handler.query("increment_used");
        this.inserLogQuery = this.handler.query("insert_log");
        this.selectLogsQuery = this.handler.query("check_logs", new ListHandler<>(new TransformerHandler<>(VoucherLogEntry.class)));
        this.userSelectQuery = this.handler.query("user_select", new TransformerHandler<>(User.class));
        this.userInsertQuery = this.handler.query("user_insert");
    }

    public void setup() {
        this.handler.query("setup").create()
                .update();
    }

    public CompletableFuture<?> insertAntidupe(UUID uuid, int amount) {
        return this.antidupeInsertQuery.createAsync()
                .update(uuid, amount, 0);
    }

    public CompletableFuture<Boolean> incrementUsed(UUID uuid) {
        return this.incrementUsedQuery.createAsync()
                .update(uuid).thenApply(count -> {
                    return count == 1;
                });
    }

    public CompletableFuture<?> insertLog(Player player, Voucher voucher, UUID uuid, String removeReason, String placeholders) {
        return CompletableFuture.runAsync(() -> {
            this.inserLogQuery.create().update(voucher.getId(), uuid, this.getUserId(player, player.getName()), new Timestamp(ZonedDateTime.now(ZoneId.systemDefault()).toInstant().toEpochMilli()), removeReason, placeholders);
        });
    }

    public CompletableFuture<List<VoucherLogEntry>> logs(String name) {
        return CompletableFuture.supplyAsync(() -> {
            List<VoucherLogEntry> entries = this.selectLogsQuery.create().query(name);
            if (entries == null || entries.isEmpty()) {
                return null;
            }

            return entries;
        }).exceptionally(throwable -> {
            LogUtils.error("An exception occurred while fetching logs!", throwable);
            return null;
        });
    }

    public Integer getUserId(String name) {
        return this.getUserId(Scheduler.get().isGlobalTickThread() ? Bukkit.getPlayer(name) : null, name);
    }

    public Integer getUserId(Player player, String name) {
        User user = this.userSelectQuery.create()
                .query(name);

        if (user != null) {
            return user.id();
        }

        if (player == null) {
            return null;
        }

        return this.userInsertQuery.create()
                .execute(player.getUniqueId(), name);
    }
}
