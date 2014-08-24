case class Player

trait Card

trait Spell extends SpellOrAbility {
    val card: Card
}

trait Permanent {
    val properties: Seq[PermanentProperty]    
}

trait PermanentProperty

trait CantAttack extends PermanentProperty
trait CantBlock extends PermanentProperty

case class AdditionalPower(value: Int) extends PermanentProperty

case class Creature(power: Int, toughness: Int, properties: Seq[PermanentProperty]) extends Permanent {
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

case class DiscardsCardEffect(card: Card) extends Effect {
    // FIXME put card on graveyard
    def affect(table: Table) = table
}

case class SequenceOfEffects(effects: Seq[Effect]) extends Effect {
    def affect(table: Table) = loop(table, effects)
    def loop(table: Table, effects: Seq[Effect]): Table =  effects match {
        case e :: es => loop(e.affect(table), es)
        case Nil => table
    }
}

trait AddsPermanentPropertyEffect[P, PP] extends Effect {
    val permanent: P
    val permanentProperty: PP
    // FIXME put property on permanent
    def affect(table: Table) = table
}

case class AddsAdditionalPowerEffect(permanent: Creature, permanentProperty: AdditionalPower)
    extends AddsPermanentPropertyEffect[Creature, AdditionalPower]

case class EntersTheBattlefield(creature: Creature) extends Effect {
    // FIXME put creature on battlefield
    def affect(table: Table) = table
}

case class CounteredSpell(countered: SpellOrAbility) extends Effect {
    // FIXME flag countered spell
    def affect(table: Table) = table
}

sealed trait SpellOrAbility {
    def resolve: Effect
}

trait InstantOrSorcery extends Spell {
    abstract override def resolve: Effect = SequenceOfEffects(DiscardsCardEffect(card) :: super.resolve :: Nil)
}

abstract class SingleTargetSpell[T](card: Card, target: T, resolveFun: (T) => Effect) extends Spell {
    def resolve = resolveFun(target)
}

case class CreatureSpell(creature: Creature) extends SpellOrAbility {
    def resolve = new EntersTheBattlefield(creature)
}

case class CounterSpell(card: Card, target: SpellOrAbility)
    extends SingleTargetSpell[SpellOrAbility](card, target, new CounteredSpell(_)) with InstantOrSorcery
