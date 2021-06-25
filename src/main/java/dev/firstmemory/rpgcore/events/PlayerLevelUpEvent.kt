package dev.firstmemory.rpgcore.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * このイベントはキャンセルできません。
 */
class PlayerLevelUpEvent(player: Player): PlayerEvent(player) {
    private val handlers = HandlerList()
    override fun getHandlers(): HandlerList { return handlers }
}