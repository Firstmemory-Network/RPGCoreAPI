package dev.firstmemory.rpgcore.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * 非同期です。
 * このイベントはキャンセルできません。
 * またオフラインの際にレベルが上った場合はプレイヤーがオンラインになった際にキャッシュされていた文が一気に上がります。
 */
class PlayerLevelUpEvent(player: Player, val level: Int): PlayerEvent(player) {
    private val handlers = HandlerList()
    override fun getHandlers(): HandlerList { return handlers }
}