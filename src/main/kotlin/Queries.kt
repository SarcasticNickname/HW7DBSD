import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.year
import org.jetbrains.exposed.sql.transactions.transaction

fun getPlayerStatisticsFor2004() {
    transaction {
        val query = Players
            .innerJoin(Results, { Players.playerId }, { Results.playerId })
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .innerJoin(Olympics, { Events.olympicId }, { Olympics.olympicId })
            .slice(
                Players.birthDate.year(),
                Players.playerId.countDistinct(), // Count distinct players
                Results.medal.count(),           // Total medals
                Results.medal.countDistinct(),   // Distinct medals
            )
            .select { Olympics.year eq 2004 }
            .groupBy(Players.birthDate.year())

        query.forEach {
            val yearOfBirth = it[Players.birthDate.year()]
            val playerCount = it[Players.playerId.countDistinct()]
            val medalCount = it[Results.medal.count()]
            println("Birth Year: $yearOfBirth, Players: $playerCount, Medals: $medalCount")
        }
    }
}


fun getTieEventsWithGoldMedals() {
    transaction {
        val eventsWithTies = Results
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .slice(Events.name, Results.eventId.count())
            .select {
                (Results.medal eq "GOLD") and (Events.isTeamEvent eq false)
            }
            .groupBy(Results.eventId, Events.name)
            .having { Results.eventId.count() greaterEq 2 }

        eventsWithTies.forEach {
            println("Event: ${it[Events.name]}, Tie count: ${it[Results.eventId.count()]}")
        }
    }
}


fun getPlayersWithMedals() {
    transaction {
        val playersWithMedals = Results
            .innerJoin(Players, { Results.playerId }, { Players.playerId })
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .innerJoin(Olympics, { Events.olympicId }, { Olympics.olympicId })
            .slice(Players.name, Olympics.olympicId)
            .select {
                Results.medal inList listOf("GOLD", "SILVER", "BRONZE")
            }
            .distinct()

        playersWithMedals.forEach {
            println("Player: ${it[Players.name]}, Olympic ID: ${it[Olympics.olympicId]}")
        }
    }
}


fun getCountryWithHighestVowelPercentage() {
    val vowels = setOf("A", "E", "I", "O", "U")

    transaction {
        // Total players per country
        val totalPlayersByCountry = Players
            .innerJoin(Countries, { Players.countryId }, { Countries.countryId })
            .slice(Countries.name, Players.playerId.count())
            .selectAll()
            .groupBy(Countries.name)
            .associate { it[Countries.name] to it[Players.playerId.count()] }

        // Players with names starting with a vowel per country
        val vowelPlayersByCountry = Players
            .innerJoin(Countries, { Players.countryId }, { Countries.countryId })
            .slice(Countries.name, Players.playerId.count())
            .select {
                Players.name.upperCase().substring(1, 1) inList vowels
            }
            .groupBy(Countries.name)
            .associate { it[Countries.name] to it[Players.playerId.count()] }

        // Calculate percentages
        val countryPercentages = totalPlayersByCountry.mapNotNull { (country, totalPlayers) ->
            val vowelPlayers = vowelPlayersByCountry[country] ?: 0
            if (totalPlayers > 0) {
                country to (vowelPlayers.toDouble() / totalPlayers * 100)
            } else null
        }

        // Find the country with the highest percentage
        val maxCountry = countryPercentages.maxByOrNull { it.second }
        maxCountry?.let {
            println("Country: ${it.first}, Percentage: ${"%.2f".format(it.second)}%")
        }
    }
}


fun getTop5CountriesByGroupMedalRatio() {
    transaction {
        val groupMedals = Results
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .innerJoin(Olympics, { Events.olympicId }, { Olympics.olympicId })
            .innerJoin(Countries, { Olympics.countryId }, { Countries.countryId })
            .slice(
                Countries.name,
                Countries.population,
                Results.medal.count()
            )
            .select {
                (Olympics.year eq 2000) and (Events.isTeamEvent eq true)
            }
            .groupBy(Countries.name, Countries.population)

        val medalRatios = groupMedals.mapNotNull {
            val country = it[Countries.name]
            val population = it[Countries.population]
            val medalCount = it[Results.medal.count()]

            if (population > 0) {
                country to (medalCount.toDouble() / population)
            } else null
        }

        val top5 = medalRatios.sortedByDescending { it.second }.take(5)

        top5.forEach {
            println("Country: ${it.first}, Medal-to-Population Ratio: ${it.second}")
        }
    }
}

