package com.licitafacil.app.data.local

import android.content.Context
import java.io.File

/**
 * Login local sem API.
 * Lê `files/local_login.txt` (2 linhas: usuário e senha).
 * Se não existir, copia de `assets/local_login.txt`.
 */
object LocalAuth {
    private const val FILE = "local_login.txt"

    fun ensureFile(context: Context) {
        val dest = File(context.filesDir, FILE)
        if (dest.exists()) return
        runCatching {
            context.assets.open(FILE).use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    fun matches(context: Context, usuario: String, senha: String): Boolean {
        ensureFile(context)
        val lines = runCatching {
            File(context.filesDir, FILE).readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
        }.getOrElse { emptyList() }
        if (lines.size < 2) return false
        return lines[0].equals(usuario.trim(), ignoreCase = true) && lines[1] == senha
    }
}
