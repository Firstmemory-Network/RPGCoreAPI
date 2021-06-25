package dev.firstmemory.rpgcore

import org.bukkit.entity.Player

interface API {
    /**
     * playerの残高にvalueを加算します。
     * @return 結果の残高を返します。
     */
    fun deposit(player: Player, value: Int): Int

    /**
     * playerの残高からvalueを減算します。
     * @return 結果の残高を返します。
     */
    fun withdrawal(player: Player, value: Int): Int

    /**
     * playerの残高を取得します。
     */
    fun getBalance(player: Player): Int

    /**
     * playerにExpを加算します。
     */
    fun addExp(player: Player, value: Int): Int

    /**
     * playerのExpからvalueを減算します。
     */
    fun removeExp(player: Player, value: Int)

    /**
     * プレイヤーのレベルを取得します。
     */
    fun getLevel(player: Player): Int

    /**
     * プレイヤーのレベルを設定します。
     */
    fun setLevel(player: Player, value: Int): Int
}