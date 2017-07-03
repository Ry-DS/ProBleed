package com.SimplyBallistic.ProBleed;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Created by SimplyBallistic on 23/06/2017.
 *
 * @author SimplyBallistic
 */
public final class PlayerUseBandageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private ItemStack itemStack;
    private boolean cancelled;


    public PlayerUseBandageEvent(Player player, ItemStack itemStack1) {
        this.player = player;
        this.itemStack = itemStack1;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }


    public Player getPlayer() {
        return player;
    }

    public ItemStack getBandage() {
        return itemStack;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
    cancelled=cancel;
    }
}
