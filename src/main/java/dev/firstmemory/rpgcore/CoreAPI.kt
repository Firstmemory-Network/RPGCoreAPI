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
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class CoreAPI(private val main: RPGCore): API {

    private val moneyCache = mutableMapOf<UUID, Int>()
    private val expCache = mutableMapOf<UUID, Int>()
    private val levelCache = mutableMapOf<UUID, Int>()
    private val skillPointCache = mutableMapOf<UUID, Int>()

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
            result.getInt("money").also { moneyCache[player.uniqueId] = it }.also {
                result.close()
            }
        } else {
            main.setupPlayer(player)
            getBalance(player)
        }
    }

    override fun getExp(player: OfflinePlayer): Int {
        expCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("exp").also { expCache[player.uniqueId] = it }.also {
                result.close()
            }
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
            result.getInt("level").also { levelCache[player.uniqueId] = it }.also {
                result.close()
            }
        } else {
            main.setupPlayer(player)
            getLevel(player)
        }
    }

    override fun getStatusPoint(player: OfflinePlayer): Int {
        skillPointCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("skill_point").also { skillPointCache[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getStatusPoint(player)
        }
    }

    override fun setStatusPoint(player: OfflinePlayer, value: Int): Int {
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("skill_point", value).send()
        }
        skillPointCache[player.uniqueId] = value
        return value
    }

    override fun setStatusLevel(player: OfflinePlayer, type: StatusType, level: Int) {
        type.caches[player.uniqueId] = level
        MultiThreadRunner {
            Update("status", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue(type.toString(), min(max(0, level), Short.MAX_VALUE.toInt())).send()
        }
    }

    override fun getStatusLevel(player: OfflinePlayer, type: StatusType): Int {
        type.caches[player.uniqueId]?.also { return it }
        val result = Select("status", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt(type.toString()).also { type.caches[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getStatusLevel(player, type)
        }
    }


    private fun setLevel(player: OfflinePlayer, level: Int) {
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId))
            .addValue("level", level)
        levelCache[player.uniqueId] = level
    }

    private fun setOldLevel(player: OfflinePlayer, level: Int) {
        Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId))
            .addValue("last_level", level).send()
    }

    fun oldToNowLevel(player: Player) {
        MultiThreadRunner {
            val now = this@CoreAPI.getLevel(player)
            val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
            result.next().takeIf(true::equals)?:return@MultiThreadRunner
            val old = result.getInt("last_level")
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
                this.setExp(player, ((getExp(player)-(levelUpCoefficient * (getLevel(player)-1))).pow(2)).toInt())
                this.isUpLevel(player)
            }
        }
    }

    init {
        main.registerEvent<PlayerJoinEvent> { oldToNowLevel(player) }
    }
}