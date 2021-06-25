package dev.firstmemory.rpgcore

import me.moru3.sqlow.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class RPGCore : JavaPlugin() {

    private val api = CoreAPI(this)

    override fun onEnable() {
        val file = dataFolder.resolve("database.db")
        if(!file.exists()) { file.createNewFile() }
        SQLow.connect(file)

        Table("userdata").also {
            it.addColumn(Column("id", DataType.INT).setPrimaryKey(true).setAutoIncrement(true).setNotNull(true))
            it.addColumn(Column("uuid", DataType.VARCHAR).setProperty(36))
            it.addColumn(Column("money", DataType.INT).setNotNull(true).setDefault(0))
            it.addColumn(Column("level", DataType.INT).setNotNull(true).setDefault(1))
        }.send(false)

        Bukkit.getOnlinePlayers().forEach(this::setupPlayer)

        setRPGCoreAPI(api)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    fun setupPlayer(player: Player) {
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