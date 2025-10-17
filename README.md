# Secure Video App 🔒

Um aplicativo Android que encripta vídeos MP4 usando criptografia AES, tornando-os reproduzíveis apenas no player customizado do app.

## 🎯 Características

- **Encriptação AES-256**: Vídeos são encriptados com AES/CBC/PKCS5Padding
- **Player Exclusivo**: Apenas o player do app consegue descriptografar e reproduzir os vídeos
- **Organização por Séries**: Crie séries, temporadas e adicione episódios de forma organizada
- **Integração Google Fotos**: Selecione vídeos diretamente do Google Fotos (sem API key necessária)
- **Material You Design**: Interface moderna usando Material3 e Jetpack Compose
- **ExoPlayer Customizado**: Player de vídeo com DataSource customizada para descriptografia em tempo real
- **Gerenciamento Completo**: Crie, organize, reproduza e exclua séries, temporadas e episódios

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

### Sistema de Organização
1. **Criar Série**: Crie uma série com título e descrição
2. **Adicionar Temporadas**: Adicione temporadas numeradas à série
3. **Adicionar Episódios**: Selecione vídeos (do dispositivo ou Google Fotos) para cada temporada
4. **Organização Automática**: Vídeos são automaticamente encriptados e organizados

### Encriptação e Reprodução
1. **Seleção de Vídeo**: Selecione vídeos da galeria, armazenamento local ou Google Fotos
2. **Encriptação Automática**: O vídeo é encriptado usando AES-256 e salvo com extensão `.enc`
3. **Armazenamento Seguro**: Vídeos encriptados são armazenados no diretório privado do app
4. **Reprodução Segura**: O player customizado usa uma `DecryptingDataSource` que descriptografa o vídeo em tempo real
5. **Proteção Total**: Outros players não conseguem reproduzir os arquivos `.enc` pois estão encriptados

### Integração Google Fotos
- O seletor de vídeos do Android mostra automaticamente vídeos do Google Fotos
- Não é necessário API key ou configuração adicional
- Funciona através do ContentResolver nativo do Android

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
│   │   ├── data/
│   │   │   └── SeriesManager.kt           # Gerenciamento de dados (JSON)
│   │   ├── models/
│   │   │   ├── Series.kt                  # Model de Série
│   │   │   ├── Season.kt                  # Model de Temporada
│   │   │   ├── Episode.kt                 # Model de Episódio
│   │   │   └── WatchProgress.kt           # Model de progresso
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt          # Tela principal
│   │   │   │   ├── SeriesListScreen.kt    # Lista de séries
│   │   │   │   ├── SeasonListScreen.kt    # Lista de temporadas
│   │   │   │   ├── EpisodeListScreen.kt   # Lista de episódios
│   │   │   │   └── PlayerScreen.kt        # Player de vídeo
│   │   │   └── theme/
│   │   │       ├── Theme.kt               # Tema Material3
│   │   │       └── Type.kt                # Tipografia
│   │   ├── VideoEncryptor.kt              # Lógica de encriptação
│   │   ├── DecryptingDataSource.kt        # DataSource customizada
│   │   ├── EncryptedVideo.kt              # Model de vídeo
│   │   └── MainActivity.kt                # Activity principal
│   ├── res/                               # Recursos
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## 🎨 Interface

- **Tela Principal**: Acesso rápido a vídeos avulsos e botão para acessar séries
- **Tela de Séries**: Grade visual com todas as séries criadas
- **Tela de Temporadas**: Lista organizada das temporadas de cada série
- **Tela de Episódios**: Lista de episódios com seleção de vídeos do Google Fotos
- **Player de Vídeo**: Player em tela cheia com controles do ExoPlayer
- **Design Material You**: Cores dinâmicas que se adaptam ao tema do sistema
- **Navegação Intuitiva**: Navegação hierárquica fácil (Séries → Temporadas → Episódios)

## ⚠️ Aviso

Este app é para fins educacionais e demonstração de conceitos de criptografia. A chave de encriptação está hardcoded no código - em produção, use métodos mais seguros de gerenciamento de chaves (Android Keystore, etc).

## 📄 Licença

Este projeto é open source e está disponível sob a licença MIT.
