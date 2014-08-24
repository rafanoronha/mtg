case class Player

abstract class Permanent(effects: Seq[Effect])

case class Creature(power: Int, toughness: Int, effects: Seq[Effect]) extends Permanent(effects) {
    def mayAttack = true
    def mayBlock = true    
}

case class Battlefield(val creatures: Seq[Creature])

case class TableSide(val battlefield: Battlefield)

// TODO implement Stack
trait Stack {
    val spells: Seq[SpellOrAbility]
    def resolve: Seq[Effect]
    def isValid(spell: SpellOrAbility) : Boolean
    def isCountered(spell: SpellOrAbility) : Boolean  
}

case class Table(sides: Seq[(TableSide, Player)], stack: Stack)

sealed trait Effect {
    def affect(table: Table) : Table
}

case class EntersTheBattlefield(creature: Creature, player: Player) extends Effect {
    // FIXME put creature on battlefield
    def affect(table: Table) = table
}

case class CounteredSpell(countered: SpellOrAbility) extends Effect {
    // FIXME flag countered spell
    def affect(table: Table) = table
}

sealed trait SpellOrAbility {
    val player: Player
    def resolve: Effect
}

trait SingleTargetSpell[T] extends SpellOrAbility {
    val target: T
}

case class CreatureSpell(player: Player, creature: Creature) extends SpellOrAbility {
    def resolve = new EntersTheBattlefield(creature, player)
}

case class CounterSpell(player: Player, target: SpellOrAbility) extends SingleTargetSpell[SpellOrAbility] {
    def resolve = new CounteredSpell(target)
}
