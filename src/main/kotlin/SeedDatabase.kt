import com.github.javafaker.Faker
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.*

val faker = Faker()

// Initialize sets for IDs to ensure uniqueness
val countryIds = mutableSetOf<String>()
val olympicIds = mutableSetOf<String>()
val playerIds = mutableSetOf<String>()
val eventIds = mutableSetOf<String>()

fun generateRandomId(length: Int, existingIds: MutableSet<String>): String {
    val chars = ('A'..'Z') + ('0'..'9')
    var id: String
    do {
        id = (1..length).map { chars.random() }.joinToString("")
    } while (id in existingIds)
    existingIds.add(id)
    return id
}

fun seedDatabase(records: Int) {
    transaction {
        repeat(records) {
            // Generate unique IDs
            val countryIdValue = generateRandomId(3, countryIds) // CHAR(3)
            val olympicIdValue = generateRandomId(7, olympicIds) // CHAR(7)
            val playerIdValue = generateRandomId(10, playerIds) // CHAR(10)
            val eventIdValue = generateRandomId(7, eventIds) // CHAR(7)

            // Insert data into the Countries table
            Countries.insert {
                it[Countries.name] = faker.country().name().take(40) // VARCHAR(40)
                it[Countries.countryId] = countryIdValue // CHAR(3)
                it[Countries.areaSqkm] = faker.number().numberBetween(1000, 1000000) // INT
                it[Countries.population] = faker.number().numberBetween(1000000, 100000000) // INT
            }

            // Generate dates
            val startDateValue = LocalDate.now().minusYears(faker.number().numberBetween(0, 100).toLong())
            val endDateValue = startDateValue.plusDays(faker.number().numberBetween(10, 20).toLong())

            // Insert data into the Olympics table
            Olympics.insert {
                it[Olympics.olympicId] = olympicIdValue // CHAR(7)
                it[Olympics.countryId] = countryIdValue // CHAR(3)
                it[Olympics.city] = faker.address().cityName().take(50) // VARCHAR(50)
                it[Olympics.year] = faker.number().numberBetween(1900, 2024) // INT
                it[Olympics.startDate] = startDateValue // DATE
                it[Olympics.endDate] = endDateValue // DATE
            }

            // Generate player's birth date
            val birthDateValue = faker.date().birthday(18, 40).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()

            // Insert data into the Players table
            Players.insert {
                it[Players.name] = faker.name().fullName().take(40) // VARCHAR(40)
                it[Players.playerId] = playerIdValue // CHAR(10)
                it[Players.countryId] = countryIdValue // CHAR(3)
                it[Players.birthDate] = birthDateValue // DATE
            }

            // Determine event type
            val isTeamEventValue = faker.bool().bool()
            val numPlayersInTeamValue = if (isTeamEventValue) faker.number().numberBetween(2, 11) else null

            // Insert data into the Events table
            Events.insert {
                it[Events.eventId] = eventIdValue // CHAR(7)
                it[Events.name] = faker.esports().event().take(40) // VARCHAR(40)
                it[Events.eventType] = faker.esports().game().take(20) // CHAR(20)
                it[Events.olympicId] = olympicIdValue // CHAR(7)
                it[Events.isTeamEvent] = isTeamEventValue // BOOLEAN
                it[Events.numPlayersInTeam] = numPlayersInTeamValue // INT
                it[Events.resultNotedIn] = "Score".take(100) // VARCHAR(100)
            }

            // Generate medal value
            val medalValue: String? = listOf("GOLD", "SILVER", "BRONZE", null).random()?.take(7) // CHAR(7)

            // Insert data into the Results table
            Results.insert {
                it[Results.eventId] = eventIdValue // CHAR(7)
                it[Results.playerId] = playerIdValue // CHAR(10)
                it[Results.medal] = medalValue // CHAR(7), can be null
                it[Results.result] = faker.number().randomDouble(2, 0, 100) // DOUBLE PRECISION
            }
        }
    }
}
