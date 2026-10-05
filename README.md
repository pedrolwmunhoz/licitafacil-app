# LicitaFacil App (Android)

App do usuário: login (`/api/pub/auth`) → configura filtros no aparelho → lista editais PNCP → detalhe.

## Rodar

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

API local no emulador: `http://10.0.2.2:8082` (`BuildConfig.API_BASE_URL`).

## Fluxo

1. Admin cria USER no painel
2. Usuário faz login no app
3. Configura palavras-chave / UF / valor (DataStore local)
4. Lista consulta PNCP (`/api/consulta/v1/contratacoes/proposta`) e filtra no aparelho
5. Detalhe + link oficial PNCP
