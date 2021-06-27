package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.events.*
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.utils.BukkitRunTask.Companion.runTask
import dev.moru3.minepie.utils.BukkitRunTask.Companion.runTaskLater
import dev.moru3.minepie.utils.Utils.Companion.isNull
import me.moru3.sqlow.Select
import me.moru3.sqlow.Update
import me.moru3.sqlow.Where
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
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
    private val statusTypes = mutableMapOf<StatusType, MutableMap<UUID, Int>>(
        StatusType.STAMINA to mutableMapOf(),
        StatusType.DEFENCE to mutableMapOf(),
        StatusType.STRENGTH to mutableMapOf(),
        StatusType.INTELLIGENCE to mutableMapOf(),
        StatusType.VOMITING to mutableMapOf()
    )
    private val maxStaminaCache = mutableMapOf<UUID, Int>()
    private val maxHealthCache = mutableMapOf<UUID, Int>()

    private val health = mutableMapOf<UUID, Int>()
    private val stamina = mutableMapOf<UUID, Int>()

    private val levelUpCoefficient = main.config.getDouble("level_up_coefficient", 10.0)

    override fun deposit(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            val event = PlayerMoneyDepositEvent(player, value)
            Bukkit.getPluginManager().callEvent(event)
            if(event.isCancelled) { return@MultiThreadRunner }
            val result = getBalance(player)+event.value
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("money", result).send()
            moneyCache[player.uniqueId] = result
        }
    }

    override fun withdrawal(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            val event = PlayerMoneyWithdrawalEvent(player, value)
            Bukkit.getPluginManager().callEvent(event)
            if(event.isCancelled) { return@MultiThreadRunner }
            val result = getBalance(player)-event.value
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("money", result).send()
            moneyCache[player.uniqueId] = result
        }
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

    override fun addExp(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            val result = getExp(player)+value
            expCache[player.uniqueId] = result
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", result).send()
            isUpLevel(player)
        }
    }

    override fun removeExp(player: OfflinePlayer, value: Int): Int {
        val result = (getExp(player)-value).takeIf { it>=0 }?:0
        setExp(player, result)
        expCache[player.uniqueId] = result
        return result
    }

    override fun setExp(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("exp", value).send()
            expCache[player.uniqueId] = value
            this.isUpLevel(player)
        }
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

    override fun addStatusPoint(player: OfflinePlayer, value: Int) {
        setStatusPoint(player, getStatusPoint(player)+value)
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

    override fun setStatusPoint(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("skill_point", value).send()
            Bukkit.getPluginManager().callEvent(PlayerStatusPointChangeEvent(player, value))
            skillPointCache[player.uniqueId] = value
        }
    }

    override fun setStatusLevel(player: OfflinePlayer, type: StatusType, level: Int) {
        statusTypes[type]?.set(player.uniqueId, level).isNull {
            statusTypes[type] = mutableMapOf(player.uniqueId to level)
        }
        MultiThreadRunner {
            Update("status", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue(type.toString(), min(max(0, level), Short.MAX_VALUE.toInt())).send()
        }
    }

    override fun getStatusLevel(player: OfflinePlayer, type: StatusType): Int {
        statusTypes[type]?.get(player.uniqueId)?.also { return it }
        val result = Select("status", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt(type.toString()).also {
                statusTypes[type]?.set(player.uniqueId, it).isNull {
                    statusTypes[type] = mutableMapOf(player.uniqueId to it)
                }
            }
        } else {
            main.setupPlayer(player)
            getStatusLevel(player, type)
        }
    }

    override fun setMaxStamina(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("max_stamina", value).send()
            Bukkit.getPluginManager().callEvent(PlayerMaxStaminaChangeEvent(player, value))
        }
        maxStaminaCache[player.uniqueId] = value
    }

    override fun getMaxStamina(player: OfflinePlayer): Int {
        maxStaminaCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("max_stamina").also { maxStaminaCache[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getMaxStamina(player)
        }
    }

    override fun setMaxHealth(player: OfflinePlayer, value: Int) {
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).addValue("max_health", value).send()
            Bukkit.getPluginManager().callEvent(PlayerMaxHealthChangeEvent(player, value))
            maxHealthCache[player.uniqueId] = value
            val onlinePlayer = player.player
            if(onlinePlayer!=null) {
                main.runTask { onlinePlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = value.toDouble() }
            }
        }
    }

    override fun getMaxHealth(player: OfflinePlayer): Int {
        maxHealthCache[player.uniqueId]?.also { return it }
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId)).send()
        return if(result.next()) {
            result.getInt("max_health").also { maxHealthCache[player.uniqueId] = it }
        } else {
            main.setupPlayer(player)
            getMaxHealth(player)
        }
    }

    override fun getStamina(player: OfflinePlayer): Int {
        return stamina[player.uniqueId]?:getMaxStamina(player).also { stamina[player.uniqueId]=it }
    }

    override fun setStamina(player: OfflinePlayer, value: Int) {
        stamina[player.uniqueId] = value
    }

    override fun setHealth(player: OfflinePlayer, value: Int) {
        health[player.uniqueId] = value
        val onlinePlayer = player.player
        if(onlinePlayer!=null) {
            onlinePlayer.health = value.toDouble()
        }
    }

    override fun getHealth(player: OfflinePlayer): Int {
        return health[player.uniqueId]?:getMaxHealth(player).also { health[player.uniqueId]=it }
    }

    @Deprecated("getSkillPoint->getStatusPoint", ReplaceWith("getStatusPoint(player)"))
    override fun getSkillPoint(player: OfflinePlayer): Int {
        return getStatusPoint(player)
    }

    @Deprecated("setSkillPoint->setStatusPoint", ReplaceWith("setStatusPoint(player, value)"))
    override fun setSkillPoint(player: OfflinePlayer, value: Int) {
        setStatusPoint(player, value)
    }

    private fun setLevel(player: OfflinePlayer, level: Int) {
        MultiThreadRunner {
            Update("userdata", Where().addKey("uuid").equals().addValue(player.uniqueId))
                .addValue("level", level)
            levelCache[player.uniqueId] = level
        }
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
                val onlinePlayer = player.player
                if(onlinePlayer!=null) {
                    Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(onlinePlayer, getLevel(player)))
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