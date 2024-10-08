package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import javax.naming.AuthenticationException

open class FTPClient : FTPClient() {
    open fun authAndConnect(connectionModel: ConnectionModel) {
        this.connect(connectionModel.host, connectionModel.port)
        if (!this.login(connectionModel.username, connectionModel.password)) {
            throw AuthenticationException("Authentication failed")
        }
        this.setFileType(FTP.BINARY_FILE_TYPE)
    }

    open fun listFilesInfo(path: String): Map<String, Long> {
        val filesInfo =
            this.listFiles(path).map {
                it.name to it.size
            }
        return filesInfo.toMap()
    }
}
