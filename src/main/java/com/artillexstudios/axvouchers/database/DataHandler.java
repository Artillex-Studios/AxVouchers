package com.artillexstudios.axvouchers.database;

import com.artillexstudios.axapi.data.ThreadedQueue;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axvouchers.AxVouchersPlugin;
import com.artillexstudios.axvouchers.database.log.VoucherLog;
import com.artillexstudios.axvouchers.voucher.Voucher;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface DataHandler {
    ThreadedQueue<Runnable> DATA_THREAD = new ThreadedQueue<>("AxVouchers-Datastore");

    static DataHandler getInstance() {
        return AxVouchersPlugin.getInstance().getDataHandler();
    }

    void setup();

    void insertAntidupe(UUID uuid, int amount);

    void incrementUsed(UUID uuid);

    boolean isDuped(UUID uuid);

    void insertLog(Player player, Voucher voucher, UUID uuid, String removeReason);

    VoucherLog getLogs(String name);

    Pair<UUID, Integer> getUserId(String name);

    void disable();
}
