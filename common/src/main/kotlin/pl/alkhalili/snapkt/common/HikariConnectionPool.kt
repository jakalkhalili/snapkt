package pl.alkhalili.snapkt.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class HikariConnectionPool(
    username: String,
    password: String,
    jdbcUrl: String
) {
    var hikariConfig: HikariConfig = HikariConfig()
    private var hikariDataSource: HikariDataSource? = null

    init {
        hikariConfig.poolName = "ProfileServiceHikariPool"
        hikariConfig.maximumPoolSize = 8
        hikariConfig.minimumIdle = 100
        hikariConfig.jdbcUrl = jdbcUrl
        hikariConfig.username = username
        hikariConfig.password = password
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")

        hikariDataSource = HikariDataSource(hikariConfig)
    }

    fun dataSource(): HikariDataSource {
        return hikariDataSource!!
    }
}
