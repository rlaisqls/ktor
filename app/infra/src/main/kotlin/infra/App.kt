/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package infra

import adapters.env.envModule
import adapters.persist.persistenceModule
import adapters.primaryweb.webBootstrap
import adapters.remoting.remotingModule
import com.github.michaelbull.logging.InlineLogger
import core.coreModule
import core.outport.BootPersistStoragePort
import core.outport.ShutdownPersistStoragePort
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import kotlinx.coroutines.runBlocking
import org.koin.core.logger.Level
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

private val logger = InlineLogger()

fun main(args: Array<String>) {
    return io.ktor.server.netty.EngineMain.main(args)
}

fun Application.main() {
    install(Koin) {
        slf4jLogger(level = Level.INFO)
        modules(
            envModule,
            coreModule,
            persistenceModule,
            remotingModule,
        )
    }

    val shutdownStoragePort = inject<ShutdownPersistStoragePort>().value
    environment.monitor.subscribe(ApplicationStopped) {
        logger.debug { "ktor server is being shutdown..." }
        shutdownStoragePort.shutdownStorage()
    }

    val bootPersistStoragePort by inject<BootPersistStoragePort>()
    runBlocking {
        bootPersistStoragePort.bootStorage {}
    }

    webBootstrap()
}