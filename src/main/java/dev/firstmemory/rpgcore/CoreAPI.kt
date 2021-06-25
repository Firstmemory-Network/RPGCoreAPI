package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.events.PlayerMoneyDepositEvent
import dev.firstmemory.rpgcore.events.PlayerMoneyWithdrawalEvent
import me.moru3.sqlow.Insert
import me.moru3.sqlow.Select
import me.moru3.sqlow.Update
import me.moru3.sqlow.Where
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

class CoreAPI(private val main: RPGCore): API {

    private val moneyCache = mutableMapOf<UUID, Int>()
    private val expCache = mutableMapOf<UUID, Int>()
    private val levelCache = mutableMapOf<UUID, Int>()

    override fun deposit(player: OfflinePlayer, value: Int): Int {
        val event = PlayerMoneyDepositEvent(player, value)
        Bukkit.getPluginManager().callEvent(event)
        if(event.isCancelled) { return getBalance(player) }
        val result = getBalance(player)+event.value
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("money", result).send()
        moneyCache[player.uniqueId] = result
        return result
    }

    override fun withdrawal(player: OfflinePlayer, value: Int): Int {
        val event = PlayerMoneyWithdrawalEvent(player, value)
        Bukkit.getPluginManager().callEvent(event)
        if(event.isCancelled) { return getBalance(player) }
        val result = getBalance(player)-event.value
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("money", result).send()
        moneyCache[player.uniqueId] = result
        return result
    }

    override fun getBalance(player: OfflinePlayer): Int {
        moneyCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("money").also { moneyCache[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getBalance(player)
        }
    }

    override fun getExp(player: OfflinePlayer): Int {
        expCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("exp").also { expCache[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getExp(player)
        }
    }

    override fun addExp(player: OfflinePlayer, value: Int): Int {
        val result = getExp(player)+value
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", result).send()
        expCache[player.uniqueId] = result
        //TODO レベルアップの機能追加
        return result
    }

    override fun removeExp(player: OfflinePlayer, value: Int): Int {
        val result = getExp(player)-value
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", result).send()
        expCache[player.uniqueId] = result
        return result
    }

    override fun setExp(player: OfflinePlayer, value: Int): Int {
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", value).send()
        expCache[player.uniqueId] = value
        //TODO レベルアップの機能追加
        return value
    }

    override fun getLevel(player: OfflinePlayer): Int {
        levelCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("level").also { levelCache[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getLevel(player)
        }
    }
}