package dev.moru3.minepie.utils

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class BukkitRunTask {
    companion object {
        fun Plugin.runTask(runnable: ()->Unit) {
            Bukkit.getScheduler().runTask(this, runnable)
        }
        fun Plugin.runTaskLater(v: Long, runnable: () -> Unit) {
            Bukkit.getScheduler().runTaskLater(this, runnable, v)
        }
    }
}