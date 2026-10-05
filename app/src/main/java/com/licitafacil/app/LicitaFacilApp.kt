package com.licitafacil.app

import android.app.Application
import com.licitafacil.app.data.local.LocalAuth
import com.licitafacil.app.data.log.AppLogger

class LicitaFacilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalAuth.ensureFile(this)
        AppLogger.init(this)
        AppLogger.event("app_start", mensagem = "LicitaFacil iniciado")
    }
}
