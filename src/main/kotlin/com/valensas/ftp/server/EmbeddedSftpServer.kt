package com.valensas.ftp.server

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import java.nio.file.Files
import java.nio.file.Path

class EmbeddedSftpServer {
    private lateinit var sshServer: SshServer
    private lateinit var serverRoot: Path

    fun start(
        username: String,
        password: String,
        host: String = "localhost",
        port: Int = 0,
        path: Path? = Files.createTempDirectory("ftp-test"),
    ) {
        sshServer = SshServer.setUpDefaultServer()
        val fileSystemFactory = VirtualFileSystemFactory()
        path?.let {
            serverRoot = it
            fileSystemFactory.defaultHomeDir = serverRoot
            sshServer.fileSystemFactory = fileSystemFactory
        }
        sshServer.keyPairProvider = setProvider()
        sshServer.subsystemFactories = listOf(SftpSubsystemFactory())
        sshServer.port = port
        sshServer.host = host
        sshServer.passwordAuthenticator =
            PasswordAuthenticator { usernameTest: String, passwordTest: String, session: ServerSession? ->
                usernameTest == username && passwordTest == password
            }
        sshServer.start()
    }

    fun stop() {
        if (::sshServer.isInitialized) {
            sshServer.stop(true)
        }
    }

    fun getHost(): String = sshServer.host

    fun getPort(): Int = sshServer.port

    private fun setProvider(): SimpleGeneratorHostKeyProvider {
        val provider = SimpleGeneratorHostKeyProvider()
        provider.algorithm = "RSA"
        provider.keySize = 2048
        return provider
    }
}
