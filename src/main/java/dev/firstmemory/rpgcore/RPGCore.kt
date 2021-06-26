package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.events.PlayerLevelUpEvent
import dev.moru3.minepie.thread.MultiThreadScheduler
import me.moru3.sqlow.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class RPGCore : JavaPlugin(), Listener {

    private val api = CoreAPI(this)

    override fun onEnable() {
        val file = dataFolder.resolve("database.db")
        if(!file.exists()) { file.createNewFile() }
        SQLow.connect(file)

        Table("userdata").also {
            it.addColumn(Column("uuid", DataType.VARCHAR).setProperty(36).setPrimaryKey(true))
            it.addColumn(Column("money", DataType.INT).setNotNull(true).setDefault(0))
            it.addColumn(Column("exp", DataType.INT).setNotNull(true).setDefault(0))
            it.addColumn(Column("level", DataType.INT).setNotNull(true).setDefault(1))
            it.addColumn(Column("last_level", DataType.INT).setNotNull(true).setDefault(1))
            it.addColumn(Column("skill_point", DataType.INT).setNotNull(true).setDefault(0))
        }.send(false)

        Bukkit.getOnlinePlayers().forEach(this::setupPlayer)

        setRPGCoreAPI(api)

        Bukkit.getOnlinePlayers().forEach(api::oldToNowLevel)

        saveDefaultConfig()
    }

    override fun onDisable() {
        MultiThreadScheduler.timers.forEach(MultiThreadScheduler::stop)
    }

    fun setupPlayer(player: OfflinePlayer) {
        Insert("userdata")
            .addValue(DataType.VARCHAR ,"uuid", player.uniqueId)
            .send(false)
    }

    companion object {
        private var api: API? = null
        private fun setRPGCoreAPI(api: API) { this.api = api }

        fun getRPGCoreAPI(): API { return api?:throw NullPointerException("api is null!") }
    }
}