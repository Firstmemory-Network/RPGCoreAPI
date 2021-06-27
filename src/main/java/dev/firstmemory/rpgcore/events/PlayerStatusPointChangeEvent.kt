package dev.firstmemory.rpgcore.events

import org.bukkit.OfflinePlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 非同期です。
 */
class PlayerStatusPointChangeEvent(player: OfflinePlayer, statusPoint: Int): Event(true) {
    private val handlers = HandlerList()
    override fun getHandlers(): HandlerList { return handlers }
}