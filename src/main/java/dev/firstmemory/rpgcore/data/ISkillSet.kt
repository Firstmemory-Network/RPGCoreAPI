package dev.firstmemory.rpgcore.data

interface ISkillSet {
    var stamina: Int
    var defence: Int
    var strength: Int
    var intelligence: Int
    var vomiting: Int

    fun reload()
}