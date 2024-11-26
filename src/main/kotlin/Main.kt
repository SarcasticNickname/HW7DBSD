import org.flywaydb.core.Flyway

fun main() {
    initDatabase()
    seedDatabase(100) // Наполняем тестовыми данными

    println("1. Individual Events with Tie Scores and Gold Medals")
    getTieEventsWithGoldMedals()

    println("\n2. Players with at least one medal")
    getPlayersWithMedals()

    println("\n3. Country with highest percentage of players starting with vowels")
    getCountryWithHighestVowelPercentage()

    println("\n4. Top 5 countries by group medal-to-population ratio (2000)")
    getTop5CountriesByGroupMedalRatio()
}