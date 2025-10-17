# Secure Video App 🔒

Um aplicativo Android que encripta vídeos MP4 usando criptografia AES, tornando-os reproduzíveis apenas no player customizado do app.

## 🎯 Características

- **Encriptação AES-256**: Vídeos são encriptados com AES/CBC/PKCS5Padding
- **Player Exclusivo**: Apenas o player do app consegue descriptografar e reproduzir os vídeos
- **Material You Design**: Interface moderna usando Material3 e Jetpack Compose
- **ExoPlayer Customizado**: Player de vídeo com DataSource customizada para descriptografia em tempo real
- **Gerenciamento de Vídeos**: Lista, reproduz e exclui vídeos encriptados facilmente

## 🛠️ Tecnologias Utilizadas

- **Kotlin** - Linguagem de programação
- **Jetpack Compose** - UI moderna e declarativa
- **Material3** - Design System do Google
- **ExoPlayer (Media3)** - Player de vídeo robusto
- **AES Encryption** - Criptografia forte para segurança dos vídeos
- **Coroutines** - Operações assíncronas

## 📦 Dependências Principais

```gradle
implementation("androidx.compose.material3:material3:1.2.0")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.media3:media3-exoplayer:1.2.1")
implementation("androidx.media3:media3-ui:1.2.1")
implementation("androidx.navigation:navigation-compose:2.7.6")
```

## 🚀 Como Funciona

1. **Seleção de Vídeo**: O usuário seleciona um vídeo MP4 da galeria
2. **Encriptação**: O vídeo é encriptado usando AES-256 e salvo com extensão `.enc`
3. **Armazenamento Seguro**: Vídeos encriptados são armazenados no diretório privado do app
4. **Reprodução**: O player customizado usa uma `DecryptingDataSource` que descriptografa o vídeo em tempo real durante a reprodução
5. **Proteção**: Outros players não conseguem reproduzir os arquivos `.enc` pois estão encriptados

## 🔐 Segurança

- Vídeos encriptados não podem ser reproduzidos por players externos (VLC, MX Player, etc.)
- Usa algoritmo AES com chave de 256 bits
- Descriptografia em tempo real durante a reprodução
- Arquivos armazenados no diretório privado do app

## 📱 Requisitos

- Android 7.0 (API 24) ou superior
- Permissões:
  - `READ_MEDIA_VIDEO` (Android 13+)
  - `READ_EXTERNAL_STORAGE` (Android 12 e inferior)

## 🏗️ Build

### Local
```bash
./gradlew assembleDebug
```

### GitHub Actions
O projeto inclui workflow do GitHub Actions que compila automaticamente o APK em pushes e pull requests para a branch `main`.

O APK compilado fica disponível nos artifacts do workflow.

## 📂 Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/movie/securevideoapp/
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt      # Tela principal com lista de vídeos
│   │   │   │   └── PlayerScreen.kt    # Tela do player
│   │   │   └── theme/
│   │   │       ├── Theme.kt           # Tema Material3
│   │   │       └── Type.kt            # Tipografia
│   │   ├── VideoEncryptor.kt          # Lógica de encriptação
│   │   ├── DecryptingDataSource.kt    # DataSource customizada para ExoPlayer
│   │   ├── EncryptedVideo.kt          # Model de dados
│   │   └── MainActivity.kt            # Activity principal
│   ├── res/                           # Recursos (strings, colors, etc)
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## 🎨 Interface

- **Tela Principal**: Lista de vídeos encriptados com opção de adicionar novos
- **Tela do Player**: Player em tela cheia com controles do ExoPlayer
- **Design Material You**: Cores dinâmicas que se adaptam ao tema do sistema

## ⚠️ Aviso

Este app é para fins educacionais e demonstração de conceitos de criptografia. A chave de encriptação está hardcoded no código - em produção, use métodos mais seguros de gerenciamento de chaves (Android Keystore, etc).

## 📄 Licença

Este projeto é open source e está disponível sob a licença MIT.
