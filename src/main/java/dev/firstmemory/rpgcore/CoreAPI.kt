package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.events.PlayerLevelUpEvent
import dev.firstmemory.rpgcore.events.PlayerMoneyDepositEvent
import dev.firstmemory.rpgcore.events.PlayerMoneyWithdrawalEvent
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.utils.BukkitRunTask.Companion.runTask
import dev.moru3.minepie.utils.BukkitRunTask.Companion.runTaskLater
import me.moru3.sqlow.Select
import me.moru3.sqlow.Update
import me.moru3.sqlow.Where
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.math.pow

class CoreAPI(private val main: RPGCore): API {

    private val moneyCache = mutableMapOf<UUID, Int>()
    private val expCache = mutableMapOf<UUID, Int>()
    private val levelCache = mutableMapOf<UUID, Int>()

    private val levelUpCoefficient = main.config.getDouble("level_up_coefficient", 10.0)

    override fun deposit(player: OfflinePlayer, value: Int): Int {
        val event = PlayerMoneyDepositEvent(player, value)
        Bukkit.getPluginManager().callEvent(event)
        if(event.isCancelled) { return getBalance(player) }
        val result = getBalance(player)+event.value
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("money", result).send()
        }
        moneyCache[player.uniqueId] = result
        return result
    }

    override fun withdrawal(player: OfflinePlayer, value: Int): Int {
        val event = PlayerMoneyWithdrawalEvent(player, value)
        Bukkit.getPluginManager().callEvent(event)
        if(event.isCancelled) { return getBalance(player) }
        val result = getBalance(player)-event.value
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("money", result).send()
        }
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
        expCache[player.uniqueId] = result
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", result).send()
        }
        isUpLevel(player)
        return result
    }

    override fun removeExp(player: OfflinePlayer, value: Int): Int {
        val result = (getExp(player)-value).takeIf { it>=0 }?:0
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", result).send()
        }
        expCache[player.uniqueId] = result
        return result
    }

    override fun setExp(player: OfflinePlayer, value: Int): Int {
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", value).send()
        expCache[player.uniqueId] = value
        this.isUpLevel(player)
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

    private fun setLevel(player: OfflinePlayer, level: Int) {
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId))
            .addValue("level", level)
    }

    private fun setOldLevel(player: OfflinePlayer, level: Int) {
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId))
            .addValue("old_level", level).send()
    }

    fun oldToNowLevel(player: Player) {
        MultiThreadRunner {
            val now = this@CoreAPI.getLevel(player)
            val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
            result.next().takeIf(true::equals)?:return@MultiThreadRunner
            val old = result.getInt("old_level")
            if(old==now) { return@MultiThreadRunner }
            main.runTaskLater(20) {
                repeat((1..now-old).count()) { main.runTaskLater(it.toLong()) { Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(player, now+it)) } }
            }
        }
    }

    private fun isUpLevel(player: OfflinePlayer) {
        //レベルアップに必要なexpが足りているかどうか
        MultiThreadRunner {
            if((levelUpCoefficient * getLevel(player)).pow(2) <= getExp(player)) {
                this.setLevel(player, getLevel(player)+1)
                if(player is Player) {
                    main.runTask {
                        Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(player, getLevel(player)))
                    }
                } else {
                    this.setOldLevel(player, getLevel(player))
                }
                this.isUpLevel(player)
            }
        }
    }

    init {
        main.registerEvent<PlayerJoinEvent> { oldToNowLevel(player) }
        main.registerEvent<PlayerQuitEvent> {
            levelCache.remove(player.uniqueId)
            moneyCache.remove(player.uniqueId)
            expCache.remove(player.uniqueId)
        }
    }
}