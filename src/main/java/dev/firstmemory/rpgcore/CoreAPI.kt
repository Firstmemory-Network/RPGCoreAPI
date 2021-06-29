package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.data.HeroData
import dev.firstmemory.rpgcore.events.PlayerLevelUpEvent
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.moru3.minepie.thread.MultiThreadRunner
import dev.moru3.minepie.utils.BukkitRunTask.Companion.runTaskLater
import me.moru3.sqlow.Insert
import me.moru3.sqlow.Select
import me.moru3.sqlow.Update
import me.moru3.sqlow.Where
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent

class CoreAPI(private val main: RPGCore): API {

    override fun deposit(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.deposit(value)
    }

    override fun withdrawal(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.withdrawal(value)
    }

    override fun getBalance(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.money?:-1
    }

    override fun getExp(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.exp?:-1
    }

    override fun addExp(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.exp = HeroData.getHeroData(player)?.exp?.plus(value)?:return
    }

    override fun removeExp(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.exp = HeroData.getHeroData(player)?.exp?.minus(value)?:return
    }

    override fun setExp(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.exp = value
    }

    override fun getLevel(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.level?:-1
    }

    override fun addStatusPoint(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.statusPoint = HeroData.getHeroData(player)?.statusPoint?.plus(value)?:return
    }

    override fun getStatusPoint(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.statusPoint?:-1
    }

    override fun setStatusPoint(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.statusPoint = value
    }

    override fun setStatusLevel(player: OfflinePlayer, type: StatusType, level: Int) {
        when(type) {
            StatusType.STAMINA -> { HeroData.getHeroData(player)?.skillSet?.stamina = level }
            StatusType.DEFENCE -> { HeroData.getHeroData(player)?.skillSet?.defence = level }
            StatusType.STRENGTH -> { HeroData.getHeroData(player)?.skillSet?.strength = level }
            StatusType.INTELLIGENCE -> { HeroData.getHeroData(player)?.skillSet?.intelligence = level }
            StatusType.VOMITING -> { HeroData.getHeroData(player)?.skillSet?.vomiting = level }
        }
    }

    override fun getStatusLevel(player: OfflinePlayer, type: StatusType): Int {
        return when(type) {
            StatusType.STAMINA -> { HeroData.getHeroData(player)?.skillSet?.stamina }
            StatusType.DEFENCE -> { HeroData.getHeroData(player)?.skillSet?.defence }
            StatusType.STRENGTH -> { HeroData.getHeroData(player)?.skillSet?.strength }
            StatusType.INTELLIGENCE -> { HeroData.getHeroData(player)?.skillSet?.intelligence }
            StatusType.VOMITING -> { HeroData.getHeroData(player)?.skillSet?.vomiting }
        }?:-1
    }

    override fun setMaxStamina(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.maxStamina = value
    }

    override fun getMaxStamina(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.maxStamina?:-1
    }

    override fun setMaxHealth(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.maxHealth = value
    }

    override fun getMaxHealth(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.maxHealth?:-1
    }

    override fun getStamina(player: OfflinePlayer): Int {
        return HeroData.getHeroData(player)?.stamina?:-1
    }

    override fun setStamina(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.stamina = value
    }

    override fun setHealth(player: OfflinePlayer, value: Int) {
        HeroData.getHeroData(player)?.health = value.toDouble()
    }

    override fun getHealth(player: OfflinePlayer): Int {
        return (HeroData.getHeroData(player)?.health?:-1.0).toInt()
    }

    @Deprecated("getSkillPoint->getStatusPoint", ReplaceWith("getStatusPoint(player)"))
    override fun getSkillPoint(player: OfflinePlayer): Int {
        return getStatusPoint(player)
    }

    @Deprecated("setSkillPoint->setStatusPoint", ReplaceWith("setStatusPoint(player, value)"))
    override fun setSkillPoint(player: OfflinePlayer, value: Int) {
        setStatusPoint(player, value)
    }

    override fun saveCustomData(player: OfflinePlayer, key: String, value: String) {
        MultiThreadRunner {
            Insert("custom_data")
                .addValue("player", player.uniqueId.toString())
                .addValue("key", key)
                .addValue("value", value)
                .send(false)
        }
    }

    override fun getCustomData(player: OfflinePlayer, key: String): String? {
        val result = Select("custom_data", Where().addKey("uuid").equals().addValue(player.uniqueId.toString()).addKey("key").equals().addValue(key)).send()
        return if(result.next()) {
            result.getString("value")
        } else {
            return null
        }
    }

    private fun setLevel(player: OfflinePlayer, level: Int) {
        HeroData.getHeroData(player)?.level = level
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

    init {
        main.registerEvent<PlayerJoinEvent> { oldToNowLevel(player) }
    }
}