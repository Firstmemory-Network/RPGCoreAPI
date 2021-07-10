package dev.moru3.RPGCoreAPI

import org.bukkit.OfflinePlayer

interface API {
    /**
     * playerの残高にvalueを加算します。
     * @return 結果の残高を返します。
     */
    fun deposit(player: OfflinePlayer, value: Int)

    /**
     * playerの残高からvalueを減算します。
     * @return 結果の残高を返します。
     */
    fun withdrawal(player: OfflinePlayer, value: Int)

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
    fun addExp(player: OfflinePlayer, value: Int)

    /**
     * playerのExpからvalueを減算します。
     */
    fun removeExp(player: OfflinePlayer, value: Int)

    /**
     * playerのexpを設定します。
     */
    fun setExp(player: OfflinePlayer, value: Int)

    /**
     * プレイヤーのレベルを取得します。
     */
    fun getLevel(player: OfflinePlayer): Int

    @Deprecated("getSkillPoint->getStatusPoint")
    fun getSkillPoint(player: OfflinePlayer): Int

    @Deprecated("setSkillPoint->setStatusPoint")
    fun setSkillPoint(player: OfflinePlayer, value: Int)

    /**
     * プレイヤーのスキルポイントを取得します。
     */
    fun getStatusPoint(player: OfflinePlayer): Int

    /**
     * プレイヤーのスキルポイントを設定します。
     */
    fun setStatusPoint(player: OfflinePlayer, value: Int)

    /**
     * ステータスポイントを追加します。
     */
    fun addStatusPoint(player: OfflinePlayer, value: Int)

    /**
     * ステータスのレベルを変更します。
     */
    fun setStatusLevel(player: OfflinePlayer, type: StatusType, level: Int)

    /**
     * ステータスのレベルを取得します。
     */
    fun getStatusLevel(player: OfflinePlayer, type: StatusType): Int

    /**
     * プレイヤーの最大スタミナを設定します。
     */
    fun setMaxStamina(player: OfflinePlayer, value: Int)

    /**
     * プレイヤーの最大スタミナを返します。
     */
    fun getMaxStamina(player: OfflinePlayer): Int

    /**
     * プレイヤーの最大HPを設定します。
     */
    fun setMaxHealth(player: OfflinePlayer, value: Int)

    /**
     * プレイヤーの最大HPを取得します。
     */
    fun getMaxHealth(player: OfflinePlayer): Int

    /**
     * スタミナを取得します。
     */
    fun getStamina(player: OfflinePlayer): Int

    /**
     * スタミナを設定します。
     */
    fun setStamina(player: OfflinePlayer, value: Int)

    /**
     * HPを設定します。
     */
    fun setHealth(player: OfflinePlayer, value: Int)

    /**
     * HPを取得します。
     */
    fun getHealth(player: OfflinePlayer): Int

    /**
     * カスタムデータを保存します。
     * 適当に保存しまくった場合消えない傷が残るので慎重に扱ってください。
     */
    fun saveCustomData(player: OfflinePlayer, key: String, value: String)

    /**
     * カスタムデータを保存します、
     * 適当に保存しまくった場合消えない傷が残るので慎重に扱ってください。
     */

    /**
     * カスタムデータを取得します。存在しない場合はnullを返します。
     */
    fun getCustomData(player: OfflinePlayer, key: String): String?
}

enum class StatusType {
    STAMINA,//スタミナ
    DEFENCE,//DEFENCE
    STRENGTH,//強さ
    INTELLIGENCE,//賢さ
    VOMITING;//ゲロの強さ??

    override fun toString(): String {
        return this.toString().toLowerCase()
    }
}