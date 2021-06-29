package dev.firstmemory.rpgcore

import dev.firstmemory.rpgcore.data.HeroData.Companion.createHeroData
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.moru3.minepie.thread.MultiThreadScheduler
import me.moru3.sqlow.*
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
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
            it.addColumn(Column("status_point", DataType.INT).setNotNull(true).setDefault(0))
            it.addColumn(Column("max_stamina", DataType.INT).setNotNull(true).setDefault(100))
            it.addColumn(Column("max_health", DataType.INT).setNotNull(true).setDefault(100))
        }.send(false)

        Table("skills").also {
            it.addColumn(Column("uuid", DataType.VARCHAR).setProperty(36).setPrimaryKey(true))
            it.addColumn(Column("stamina", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("defence", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("strength", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("intelligence", DataType.SMALLINT).setDefault(10).setNotNull(true))
            it.addColumn(Column("vomiting", DataType.SMALLINT).setDefault(10).setNotNull(true))
        }.send(false)

        Table("custom_data").also {
            it.addColumn(Column("uuid", DataType.VARCHAR).setNotNull(false))
            it.addColumn(Column("key", DataType.TEXT).setPrimaryKey(true).setNotNull(true))
            it.addColumn(Column("value", DataType.TEXT).setNotNull(false))
        }.send(false)

        api = CoreAPI(this)

        setRPGCoreAPI(api!!)

        Bukkit.getOnlinePlayers().forEach { this.createHeroData(it) }

        this.registerEvent<PlayerJoinEvent> { this@RPGCore.createHeroData(this.player) }
    }

    override fun onDisable() {
        SQLow.getConnection().close()
        MultiThreadScheduler.timers.forEach(MultiThreadScheduler::stop)
    }

    companion object {
        private var api: API? = null
        private fun setRPGCoreAPI(api: API) { this.api = api }

        fun getRPGCoreAPI(): API {
            return api?:throw NullPointerException("Please refer to it after starting the API.")
        }
    }
}