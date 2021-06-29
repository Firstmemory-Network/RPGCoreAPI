package dev.firstmemory.rpgcore.data

import dev.firstmemory.rpgcore.events.*
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.utils.BukkitRunTask.Companion.runTaskLater
import me.moru3.sqlow.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.math.pow

class HeroData(val plugin: Plugin, val uuid: UUID): IHeroData {

    override var money: Int = 0
        set(value) {
            MultiThreadRunner {
                if(value==field) { return@MultiThreadRunner }
                if(value>field) {
                    //deposit
                    val event = PlayerMoneyDepositEvent(Bukkit.getOfflinePlayer(uuid), value)
                    Bukkit.getPluginManager().callEvent(event)
                    if(event.isCancelled) { return@MultiThreadRunner }
                } else {
                    val event = PlayerMoneyWithdrawalEvent(Bukkit.getOfflinePlayer(uuid), value)
                    Bukkit.getPluginManager().callEvent(event)
                    if(event.isCancelled) { return@MultiThreadRunner }
                }
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("money", value).send()
                field = value
            } }

    override var exp: Int = 0
        set(value) {
            MultiThreadRunner {
                if(value==field) { return@MultiThreadRunner }
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                val event = PlayerMoneyDepositEvent(offlinePlayer, value)
                Bukkit.getPluginManager().callEvent(event)
                if(event.isCancelled) { return@MultiThreadRunner }
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("exp", value).send()
                field = value
            }
        }
    override var level: Int = 0
        set(value) {
            MultiThreadRunner {
                if(value<=field) { return@MultiThreadRunner }
                val onlinePlayer = Bukkit.getOfflinePlayer(uuid).player
                if(onlinePlayer!=null) {
                    Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(onlinePlayer, value))
                    oldLevel = value
                }
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("level", value).send()
                field = value
            }
        }
    override var maxStamina: Int = 0
        set(value) {
            MultiThreadRunner {
                if(value<=field) { return@MultiThreadRunner }
                Bukkit.getPluginManager().callEvent(PlayerMaxStaminaChangeEvent(Bukkit.getOfflinePlayer(uuid), value))
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("max_stamina", value).send()
                field = value
            }
        }

    override var maxHealth: Int = 0
        set(value) {
            Bukkit.getOfflinePlayer(uuid).player?.also { player ->
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth.toDouble()
            }
            MultiThreadRunner {
                if(value<=field) { return@MultiThreadRunner }
                Bukkit.getPluginManager().callEvent(PlayerMaxHealthChangeEvent(Bukkit.getOfflinePlayer(uuid), value))
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("max_health", value).send()
                field = value
            }
        }

    override var statusPoint: Int = 0
        set(value) {
            MultiThreadRunner {
                if(value<=field) { return@MultiThreadRunner }
                Bukkit.getPluginManager().callEvent(PlayerStatusPointChangeEvent(Bukkit.getOfflinePlayer(uuid), value))
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("status_point", value).send()
                field = value
            }
        }

    override val skillSet: ISkillSet = SkillSet(this)
    override val customData: MutableMap<String, String> = mutableMapOf()

    /**
     * real time var
     */
    override var health: Double = 1.0
        set(value) {
            Bukkit.getOfflinePlayer(uuid).player?.also { player ->
                player.health = value
            }
            field = value
        }
    override var stamina: Int = 1

    override fun deposit(value: Int) {
        money+=value
    }

    override fun withdrawal(value: Int) {
        money-=value
    }

    var oldLevel: Int = 0
        set(value) {
            Update("userdata", Where().addKey("uuid").equals().addValue(uuid))
                .addValue("last_level", level).send()
            field = value
        }

    fun checkLevelUp(): Boolean {
        return if((10.0 * level).pow(2) <= exp) {
            level+=1
            exp = ((exp-(10.0 * (level-1))).pow(2)).toInt()
            checkLevelUp()
            true
        } else {
            false
        }
    }

    private fun reSetup() {
        Insert("userdata")
            .addValue(DataType.VARCHAR ,"uuid", uuid)
            .send(false)
        Insert("status")
            .addValue(DataType.VARCHAR ,"uuid", uuid)
            .send(false)
    }

    private fun syncOldLevel() {
        MultiThreadRunner {
            val onlinePlayer = Bukkit.getOfflinePlayer(uuid).player?:return@MultiThreadRunner
            if(oldLevel==level) { return@MultiThreadRunner }
            plugin.runTaskLater(20) {
                repeat((1..level-oldLevel).count()) { plugin.runTaskLater(it.toLong()) { Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(onlinePlayer, level+it)) } }
            }
        }
    }

    override fun reload() {
        reSetup()
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(uuid)).send()
        if(!result.next()) throw NullPointerException()
        money = result.getInt("money")
        exp = result.getInt("exp")
        level = result.getInt("level")
        maxHealth = result.getInt("max_health")
        maxStamina = result.getInt("max_stamina")
        statusPoint = result.getInt("status_point")
        oldLevel = result.getInt("old_level")
    }

    init {
        reload()
        health = maxHealth.toDouble()
        stamina = maxStamina
        syncOldLevel()
        plugin.registerEvent<PlayerJoinEvent> { if(this.player.uniqueId==uuid) { syncOldLevel() } }
    }

    companion object {

        private val data = mutableMapOf<UUID, IHeroData>()

        fun Plugin.createHeroData(player: Player): IHeroData {
            data[player.uniqueId]?.also { return it }
            return HeroData(this, player.uniqueId)
        }

        fun getHeroData(offlinePlayer: OfflinePlayer): IHeroData? {
            return data[offlinePlayer.uniqueId]
        }
    }
}