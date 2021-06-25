package dev.firstmemory.rpgcore

import org.bukkit.entity.Player

class CoreAPI(private val main: RPGCore): API {
    override fun deposit(player: Player, value: Int): Int {
        TODO("Not yet implemented")
    }

    override fun withdrawal(player: Player, value: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getBalance(player: Player): Int {
        TODO("Not yet implemented")
    }

    override fun addExp(player: Player, value: Int): Int {
        TODO("Not yet implemented")
    }

    override fun removeExp(player: Player, value: Int) {
        TODO("Not yet implemented")
    }

    override fun getLevel(player: Player): Int {
        TODO("Not yet implemented")
    }

    override fun setLevel(player: Player, value: Int): Int {
        TODO("Not yet implemented")
    }
}