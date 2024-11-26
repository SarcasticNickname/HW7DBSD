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
                Players.playerId.countDistinct().alias("player_count"),
                Results.medal.count().alias("gold_medal_count")
            )
            .select { (Olympics.year eq 2004) and (Results.medal eq "GOLD") }
            .groupBy(Players.birthDate.year())

        query.forEach {
            val yearOfBirth = it[Players.birthDate.year()]
            val playerCount = it[Players.playerId.countDistinct().alias("player_count")]
            val goldMedalCount = it[Results.medal.count().alias("gold_medal_count")]
            println("Birth Year: $yearOfBirth, Players: $playerCount, Gold Medals: $goldMedalCount")
        }
    }
}

fun getTieEventsWithGoldMedals() {
    transaction {
        // Определяем агрегатную функцию для подсчёта количества золотых медалистов
        val goldWinnersCount = Results.playerId.count()

        // Строим запрос
        val query = Results
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .slice(Events.name, goldWinnersCount)
            .select {
                (Results.medal eq "GOLD") and (Events.isTeamEvent eq false)
            }
            .groupBy(Results.eventId, Events.name)
            .having { goldWinnersCount greaterEq longLiteral(2) }

        // Выполняем запрос и обрабатываем результаты
        query.forEach {
            val eventName = it[Events.name]
            val tieCount = it[goldWinnersCount]
            println("Event: $eventName, Tie count: $tieCount")
        }
    }
}

fun getPlayersWithMedals() {
    transaction {
        val medalCount = Results.medal.countDistinct().alias("medal_count")

        val query = Results
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .select {
                Results.medal.inList(listOf("GOLD", "SILVER", "BRONZE"))
            }
            .groupBy(Results.playerId, Events.olympicId)
            .having { medalCount eq longLiteral(3) }
            .adjustSlice { slice(Results.playerId, Events.olympicId) }

        query.forEach {
            val playerId = it[Results.playerId]
            val olympicId = it[Events.olympicId]
            val playerName = Players
                .select { Players.playerId eq playerId }
                .singleOrNull()?.get(Players.name) ?: "Unknown Player"
            println("Player: $playerName, Olympic ID: $olympicId")
        }
    }
}


fun getCountryWithHighestVowelPercentage() {
    val vowels = listOf("A", "E", "I", "O", "U")

    transaction {
        // Общее количество игроков по странам
        val totalPlayersByCountry = Players
            .innerJoin(Countries, { Players.countryId }, { Countries.countryId })
            .slice(Countries.name, Players.playerId.count())
            .selectAll()
            .groupBy(Countries.name)
            .associate { it[Countries.name] to it[Players.playerId.count()] }

        // Количество игроков с именами, начинающимися на гласную, по странам
        val vowelPlayersByCountry = Players
            .innerJoin(Countries, { Players.countryId }, { Countries.countryId })
            .slice(Countries.name, Players.playerId.count())
            .select {
                // Используем функцию substring(1, 1) для получения первого символа (SQL использует 1-индексацию)
                Players.name.upperCase().substring(1, 1) inList vowels
            }
            .groupBy(Countries.name)
            .associate { it[Countries.name] to it[Players.playerId.count()] }

        // Расчёт процентов
        val countryPercentages = totalPlayersByCountry.mapNotNull { (country, totalPlayers) ->
            val vowelPlayers = vowelPlayersByCountry[country] ?: 0
            if (totalPlayers > 0) {
                country to (vowelPlayers.toDouble() / totalPlayers * 100)
            } else null
        }

        // Нахождение страны с наибольшим процентом
        val maxCountry = countryPercentages.maxByOrNull { it.second }
        maxCountry?.let {
            println("Country: ${it.first}, Percentage: ${"%.2f".format(it.second)}%")
        }
    }
}


fun getTop5CountriesByGroupMedalRatio() {
    transaction {
        val medalCountAlias = Results.medal.count().alias("medal_count")

        val groupMedals = Results
            .innerJoin(Events, { Results.eventId }, { Events.eventId })
            .innerJoin(Olympics, { Events.olympicId }, { Olympics.olympicId })
            .innerJoin(Players, { Results.playerId }, { Players.playerId })
            .innerJoin(Countries, { Players.countryId }, { Countries.countryId })
            .slice(
                Countries.name,
                Countries.population,
                medalCountAlias
            )
            .select {
                (Olympics.year eq 2000) and (Events.isTeamEvent eq true)
            }
            .groupBy(Countries.name, Countries.population)

        val medalRatios = groupMedals.mapNotNull {
            val country = it[Countries.name]
            val population = it[Countries.population]
            val medalCount = it[medalCountAlias]

            if (population > 0) {
                country to (medalCount.toDouble() / population)
            } else null
        }

        val top5 = medalRatios.sortedBy { it.second }.take(5)

        top5.forEach {
            println("Country: ${it.first}, Medal-to-Population Ratio: ${it.second}")
        }
    }
}