package dev.firstmemory.rpgcore.data

import dev.moru3.minepie.thread.MultiThreadRunner
import me.moru3.sqlow.Select
import me.moru3.sqlow.Update
import me.moru3.sqlow.Where

open class SkillSet(private val heroData: HeroData): ISkillSet {
    override var stamina: Int = 0
        set(value) {
            MultiThreadRunner {
                Update("skills", Where().addKey("uuid").equals().addValue(heroData.uuid))
                    .addValue("stamina", heroData.level).send()
                field = value
            }
        }
    override var defence: Int = 0
        set(value) {
            MultiThreadRunner {
                Update("skills", Where().addKey("uuid").equals().addValue(heroData.uuid))
                    .addValue("defence", heroData.level).send()
                field = value
            }
        }
    override var strength: Int = 0
        set(value) {
            MultiThreadRunner {
                Update("skills", Where().addKey("uuid").equals().addValue(heroData.uuid))
                    .addValue("strength", heroData.level).send()
                field = value
            }
        }
    override var intelligence: Int = 0
        set(value) {
            MultiThreadRunner {
                Update("skills", Where().addKey("uuid").equals().addValue(heroData.uuid))
                    .addValue("intelligence", heroData.level).send()
                field = value
            }
        }
    override var vomiting: Int = 0
        set(value) {
            MultiThreadRunner {
                Update("skills", Where().addKey("uuid").equals().addValue(heroData.uuid))
                    .addValue("vomiting", heroData.level).send()
                field = value
            }
        }

    final override fun reload() {
        heroData.reload()
        val result = Select("skills", Where().addKey("uuid").equals().addValue(heroData.uuid)).send()
        if(!result.next()) { throw NullPointerException() }
        stamina = result.getInt("stamina")
        defence = result.getInt("defence")
        strength = result.getInt("strength")
        intelligence = result.getInt("intelligence")
        vomiting = result.getInt("vomiting")
    }

    init { reload() }
}