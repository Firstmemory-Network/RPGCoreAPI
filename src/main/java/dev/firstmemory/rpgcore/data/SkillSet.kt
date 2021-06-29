package dev.firstmemory.rpgcore.data

import me.moru3.sqlow.Select
import me.moru3.sqlow.Where

open class SkillSet(private val heroData: HeroData): ISkillSet {
    override var stamina: Int = 0
    override var defence: Int = 0
    override var strength: Int = 0
    override var intelligence: Int = 0
    override var vomiting: Int = 0

    final override fun reload() {
        val result = Select("stamina", Where().addKey("uuid").equals().addValue(heroData.uuid)).send()
        if(!result.next()) { throw NullPointerException() }
        stamina = result.getInt("stamina")
        defence = result.getInt("defence")
        strength = result.getInt("strength")
        intelligence = result.getInt("intelligence")
        vomiting = result.getInt("vomiting")
    }

    init { reload() }
}

interface ISkillSet {
    var stamina: Int
    var defence: Int
    var strength: Int
    var intelligence: Int
    var vomiting: Int

    fun reload()
}