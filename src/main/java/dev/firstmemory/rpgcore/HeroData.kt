package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.events.PlayerLevelUpEvent
import dev.firstmemory.rpgcore.events.PlayerMoneyDepositEvent
import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.utils.Utils.Companion.isNull
import me.moru3.sqlow.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import kotlin.math.pow

class HeroData(val main: RPGCore, val uuid: UUID) {
    val levelUpCoefficient = main.levelUpCoefficient

    var money: Int = 0
        set(value) {
            MultiThreadRunner {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                val event = PlayerMoneyDepositEvent(offlinePlayer, value)
                Bukkit.getPluginManager().callEvent(event)
                if(event.isCancelled) { return@MultiThreadRunner }
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("money", value).send()
                field = value
            } }

    var exp: Int = 0
        set(value) {
            MultiThreadRunner {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                val event = PlayerMoneyDepositEvent(offlinePlayer, value)
                Bukkit.getPluginManager().callEvent(event)
                if(event.isCancelled) { return@MultiThreadRunner }
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("exp", value).send()
                field = value
            }
        }
    var level: Int = 0
        set(value) {
            if(value<=field) { return }
            MultiThreadRunner {
                val onlinePlayer = Bukkit.getOfflinePlayer(uuid).player
                if(onlinePlayer!=null) {
                    Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(onlinePlayer, value))
                    setOldLevel(value)
                }
                Update("userdata", Where().addKey("uuid").equals().addValue(uuid)).addValue("level", value).send()
                field = value
            }
        }
    var maxStamina: Int
    var maxHealth: Int

    private val skillPoint: MutableMap<StatusType, Int> = mutableMapOf()
    private val customData: MutableMap<String, String> = mutableMapOf()

    /**
     * real time var
     */
    var health: Int
    var stamina: Int

    fun checkLevelUp(): Boolean {
        if((levelUpCoefficient * level).pow(2) <= exp) {
            level+=1
            exp = ((exp-(levelUpCoefficient * (level-1))).pow(2)).toInt()
            checkLevelUp()
            return true
        } else {
            return false
        }
    }

    fun reSetup() {
        Insert("userdata")
            .addValue(DataType.VARCHAR ,"uuid", uuid)
            .send(false)
        Insert("status")
            .addValue(DataType.VARCHAR ,"uuid", uuid)
            .send(false)
    }

    private fun setOldLevel(level: Int) {
        Update("userdata", Where().addKey("uuid").equals().addValue(uuid))
            .addValue("last_level", level).send()
    }

    fun reload() {
        Select("userdata", Where().addKey("uuid").equals().addValue(uuid)).send().also {
            if(!it.next()) throw NullPointerException()
            money = it.getInt("money")
        }

    }

    init {
        money-=10
    }
}