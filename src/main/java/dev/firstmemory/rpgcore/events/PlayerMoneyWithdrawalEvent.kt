package dev.firstmemory.rpgcore.events

import org.bukkit.OfflinePlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 非同期です。
 */
class PlayerMoneyWithdrawalEvent(val player: OfflinePlayer, var value: Int): Event(), Cancellable {

    private var cancelled = false
    override fun isCancelled(): Boolean { return cancelled }

    override fun setCancelled(p0: Boolean) { cancelled = p0 }

    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList { return handlers }

}