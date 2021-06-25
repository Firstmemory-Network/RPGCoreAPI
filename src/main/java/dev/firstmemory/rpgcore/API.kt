package dev.firstmemory.rpgcore

import org.bukkit.OfflinePlayer

interface API {
    /**
     * playerの残高にvalueを加算します。
     * @return 結果の残高を返します。
     */
    fun deposit(player: OfflinePlayer, value: Int): Int

    /**
     * playerの残高からvalueを減算します。
     * @return 結果の残高を返します。
     */
    fun withdrawal(player: OfflinePlayer, value: Int): Int

    /**
     * playerの残高を取得します。
     */
    fun getBalance(player: OfflinePlayer): Int

    /**
     * playerのExpを取得します。
     */
    fun getExp(player: OfflinePlayer): Int

    /**
     * playerにExpを加算します。
     */
    fun addExp(player: OfflinePlayer, value: Int): Int

    /**
     * playerのExpからvalueを減算します。
     */
    fun removeExp(player: OfflinePlayer, value: Int): Int

    /**
     * playerのexpを設定します。
     */
    fun setExp(player: OfflinePlayer, value: Int): Int

    /**
     * プレイヤーのレベルを取得します。
     */
    fun getLevel(player: OfflinePlayer): Int
}