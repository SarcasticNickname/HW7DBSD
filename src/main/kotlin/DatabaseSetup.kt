import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun initDatabase() {
    try {
        println("Попытка подключения к базе данных...")
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/olympics_db",
            driver = "org.postgresql.Driver",
            user = "olympic_user",
            password = "olympic_pass"
        )
        println("Подключение к базе данных успешно!")

        val flyway = Flyway.configure()
            .dataSource("jdbc:postgresql://localhost:5432/olympics_db", "olympic_user", "olympic_pass")
            .load()


        flyway.migrate()
        println("Миграции Flyway успешно выполнены!")
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
        e.printStackTrace()
    }
}
