package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.events.PlayerLevelUpEvent
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
            it.addColumn(Column("id", DataType.INT).setPrimaryKey(true).setAutoIncrement(true).setNotNull(true))
            it.addColumn(Column("uuid", DataType.VARCHAR).setProperty(36))
            it.addColumn(Column("money", DataType.INT).setNotNull(true).setDefault(0))
            it.addColumn(Column("exp", DataType.INT).setNotNull(true).setDefault(0))
            it.addColumn(Column("level", DataType.INT).setNotNull(true).setDefault(1))
            it.addColumn(Column("last_level", DataType.INT).setNotNull(true).setDefault(1))
        }.send(false)

        Bukkit.getOnlinePlayers().forEach(this::setupPlayer)

        setRPGCoreAPI(api)

        Bukkit.getPluginManager().registerEvents(this, this)

        Bukkit.getOnlinePlayers().forEach { onJoin(PlayerJoinEvent(it, null)) }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        Update("userdata", Where().addKey("uuid").equals().addValue(event.player.uniqueId))
            .addValue("old_level", api.getLevel(event.player)).send()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val now = api.getLevel(event.player)
        val result = Select("userdata", Where().addKey("uuid").equals().addValue(event.player.uniqueId)).send()
        result.next().takeIf(true::equals)?:return
        val old = result.getInt("old_level")
        if(old==now) { return }
        repeat((1..now-old).count()) {
            Bukkit.getPluginManager().callEvent(PlayerLevelUpEvent(event.player))
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
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