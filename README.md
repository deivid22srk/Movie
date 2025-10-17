# Secure Video App 📺

Um aplicativo Android para organizar e assistir séries através de streaming direto do Google Fotos, com sistema de progresso e backup automático no Google Drive.

## 🎯 Características

- **Streaming do Google Fotos**: Reproduza vídeos diretamente do Google Fotos sem precisar encriptar ou duplicar
- **Organização por Séries**: Crie séries, temporadas e adicione episódios de forma organizada
- **Progresso Automático**: Continue assistindo de onde parou - o progresso é salvo automaticamente
- **Backup no Google Drive**: Faça backup e restaure todos os dados no Google Drive (sem API key!)
- **Material You Design**: Interface moderna usando Material3 e Jetpack Compose
- **Streaming Eficiente**: Não ocupa espaço extra no dispositivo, vídeos ficam no Google Fotos
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
implementation("com.google.code.gson:gson:2.10.1")
```

## 🚀 Como Funciona

### Sistema de Organização
1. **Criar Série**: Crie uma série com título e descrição
2. **Adicionar Temporadas**: Adicione temporadas numeradas à série
3. **Adicionar Episódios**: Selecione vídeos (do dispositivo ou Google Fotos) para cada temporada
4. **Organização Automática**: Vídeos são automaticamente encriptados e organizados

### Streaming e Progresso
1. **Seleção de Vídeo**: Selecione vídeos da galeria ou Google Fotos (aparecem automaticamente)
2. **Sem Duplicação**: O vídeo permanece no Google Fotos, apenas a referência é salva
3. **Streaming Direto**: Reprodução via URI persistente - não ocupa espaço no dispositivo
4. **Salvamento Automático**: A cada 5 segundos o progresso é salvo automaticamente
5. **Retomada Automática**: Ao abrir o episódio novamente, continua de onde parou
6. **Indicador Visual**: Barra de progresso mostra quanto foi assistido de cada episódio

### Integração Google Fotos
- O seletor de vídeos do Android mostra automaticamente vídeos do Google Fotos
- Não é necessário API key ou configuração adicional
- Funciona através do ContentResolver nativo do Android
- Usa URI persistente para manter acesso aos vídeos

### Backup e Restore (Google Drive)
1. **Criar Backup**: Toque em "Fazer Backup no Google Drive" nas configurações
2. **Seletor do Sistema**: Escolha a pasta do Google Drive onde salvar
3. **Arquivo ZIP**: O backup é salvo como arquivo .zip contendo todos os dados
4. **Restaurar**: Toque em "Restaurar do Google Drive" e selecione o arquivo de backup
5. **Sem API Key**: Usa Storage Access Framework (SAF) nativo do Android
6. **Dados Incluídos**: Séries, temporadas, episódios, progresso de visualização

## 💾 Armazenamento

- Vídeos permanecem no Google Fotos (não duplica arquivos)
- Apenas referências (URIs) são salvas no app
- Economiza espaço no dispositivo
- Dados organizacionais salvos em JSON no diretório privado do app
- Backup completo pode ser salvo no Google Drive

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
│   │   │   ├── SeriesManager.kt           # Gerenciamento de dados (JSON)
│   │   │   └── BackupManager.kt           # Sistema de backup/restore
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
│   │   │   │   ├── PlayerScreen.kt        # Player de vídeo com progresso
│   │   │   │   └── SettingsScreen.kt      # Configurações e backup
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
- **Tela de Episódios**: Lista de episódios com barra de progresso e indicador de "assistido"
- **Player de Vídeo**: Player em tela cheia que salva progresso automaticamente
- **Tela de Configurações**: Backup/restore do Google Drive e informações do app
- **Design Material You**: Cores dinâmicas que se adaptam ao tema do sistema
- **Navegação Intuitiva**: Navegação hierárquica fácil (Séries → Temporadas → Episódios)

## ⚠️ Importante

- Os vídeos precisam estar sincronizados no Google Fotos para aparecerem no seletor
- O app tenta obter permissão persistente para as URIs, mas alguns provedores (como Google Fotos) não permitem
- Se um vídeo for removido do Google Fotos ou a permissão expirar, ele não poderá mais ser reproduzido
- **Recomendação**: Para garantir acesso permanente, mantenha os vídeos sincronizados no Google Fotos
- O backup salva apenas os dados organizacionais (séries, temporadas, progresso), não os vídeos em si
- Para uso completo, certifique-se de ter o Google Fotos instalado e sincronizado

### Limitação do Google Fotos
O Google Fotos não permite que apps de terceiros mantenham acesso permanente aos vídeos via URI. Isso significa que:
- Os vídeos funcionam perfeitamente enquanto o app está em uso
- Após algum tempo (dias/semanas), o Android pode revogar o acesso à URI
- Se isso acontecer, você precisará "re-adicionar" o episódio selecionando o vídeo novamente
- Os dados da série, temporada e progresso são mantidos, apenas a referência ao vídeo precisa ser atualizada

## 📄 Licença

Este projeto é open source e está disponível sob a licença MIT.
