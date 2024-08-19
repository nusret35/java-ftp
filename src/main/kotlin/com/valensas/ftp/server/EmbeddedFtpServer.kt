package com.valensas.ftp.server

import com.valensas.ftp.model.ConnectionType
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import java.io.File
import java.nio.file.Files

class EmbeddedFtpServer {
    private lateinit var ftpServer: FtpServer
    private lateinit var listenerFactory: ListenerFactory

    fun start(
        username: String,
        password: String,
        type: ConnectionType,
        port: Int = 990,
        isImplicit: Boolean = false,
        certificatePath: String? = null,
    ) {
        val serverRoot = Files.createTempDirectory("ftp-test")
        val serverFactory = FtpServerFactory()
        listenerFactory = ListenerFactory()
        if (type == ConnectionType.FTPS) {
            val ssl = SslConfigurationFactory()
            certificatePath?.let {
                ssl.keystoreFile = File(it)
            }
            ssl.keystorePassword = password
            listenerFactory.sslConfiguration = ssl.createSslConfiguration()
            listenerFactory.isImplicitSsl = isImplicit
        }
        listenerFactory.port = port
        val userManagerFactory = PropertiesUserManagerFactory()
        val userManager = userManagerFactory.createUserManager()

        val user = BaseUser()
        user.name = username
        user.password = password
        user.homeDirectory = serverRoot.toAbsolutePath().toString()

        userManager.save(user)

        serverFactory.addListener("default", listenerFactory.createListener())
        serverFactory.userManager = userManager
        ftpServer = serverFactory.createServer()
        ftpServer.start()
    }

    fun stop() {
        if (::ftpServer.isInitialized) {
            ftpServer.stop()
        }
    }

    fun getPort(): Int = listenerFactory.port
}
