package dev.firstmemory.rpgcore

import dev.moru3.minepie.thread.MultiThreadScheduler
import me.moru3.sqlow.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin

class RPGCore : JavaPlugin() {

    private var api: CoreAPI? = null

    override fun onEnable() {
        saveDefaultConfig()

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

        Table("status").also {
            it.addColumn(Column("uuid", DataType.VARCHAR).setProperty(36).setPrimaryKey(true))
            it.addColumn(Column("stamina", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("defence", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("strength", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("intelligence", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("vomiting", DataType.SMALLINT).setDefault(10).setNotNull(true))
        }

        Bukkit.getOnlinePlayers().forEach(this::setupPlayer)

        api = CoreAPI(this)

        setRPGCoreAPI(api!!)

        Bukkit.getOnlinePlayers().forEach(api!!::oldToNowLevel)
    }

    override fun onDisable() {
        SQLow.getConnection().close()
        MultiThreadScheduler.timers.forEach(MultiThreadScheduler::stop)
    }

    fun setupPlayer(player: OfflinePlayer) {
        Insert("userdata")
            .addValue(DataType.VARCHAR ,"uuid", player.uniqueId)
            .send(false)
        Insert("status")
            .addValue(DataType.VARCHAR ,"uuid", player.uniqueId)
            .send(false)
    }

    companion object {
        private var api: API? = null
        private fun setRPGCoreAPI(api: API) { this.api = api }

        fun getRPGCoreAPI(): API {
            return api?:throw NullPointerException("Please refer to it after starting the API.")
        }
    }
}