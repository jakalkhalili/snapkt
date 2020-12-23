package pl.alkhalili.snapkt.common

import org.flywaydb.core.Flyway
import javax.sql.DataSource

object Migration {
    fun migrate(dataSource: DataSource) {
        Flyway.configure().dataSource(dataSource).load().migrate()
    }
}
