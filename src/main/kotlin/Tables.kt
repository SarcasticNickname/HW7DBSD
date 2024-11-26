import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object Countries : Table("countries") {
    val countryId = char("country_id", 3).uniqueIndex()
    val name = varchar("name", 40)
    val areaSqkm = integer("area_sqkm")
    val population = integer("population")
    override val primaryKey = PrimaryKey(countryId)
}

object Olympics : Table("olympics") {
    val olympicId = char("olympic_id", 7).uniqueIndex()
    val countryId = reference("country_id", Countries.countryId)
    val city = varchar("city", 50)
    val year = integer("year")
    val startDate = date("startdate")
    val endDate = date("enddate")
    override val primaryKey = PrimaryKey(olympicId)
}

object Players : Table("players") {
    val playerId = char("player_id", 10).uniqueIndex()
    val name = varchar("name", 40)
    val countryId = reference("country_id", Countries.countryId)
    val birthDate = date("birthdate")
    override val primaryKey = PrimaryKey(playerId)
}

object Events : Table("events") {
    val eventId = char("event_id", 7).uniqueIndex()
    val name = varchar("name", 40)
    val eventType = varchar("eventtype", 20)
    val olympicId = reference("olympic_id", Olympics.olympicId)
    val isTeamEvent = bool("is_team_event")
    val numPlayersInTeam = integer("num_players_in_team").nullable()
    val resultNotedIn = varchar("result_noted_in", 100)
    override val primaryKey = PrimaryKey(eventId)
}

object Results : Table("results") {
    val eventId = reference("event_id", Events.eventId)
    val playerId = reference("player_id", Players.playerId)
    val medal = varchar("medal", 7).nullable()
    val result = double("result").nullable()
    override val primaryKey = PrimaryKey(eventId, playerId) // Композитный ключ
}
