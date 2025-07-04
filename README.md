# MostSave - Secure Password Manager

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="MostSave Logo" width="120" height="120">
  
  **Your Passwords Stay Here - Not Online**
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
  [![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
  [![Version](https://img.shields.io/badge/Version-2.1.11-blue.svg)](https://github.com/Abhibth0/Most_Save)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/Abhibth0/Most_Save/blob/main/LICENSE)
</div>

## 🔐 Overview

MostSave is a secure, feature-rich password manager for Android that prioritizes your privacy and security. With military-grade encryption and biometric authentication, your passwords remain completely offline and under your control.

## ✨ Key Features

### 🛡️ Security First
- **Offline Storage**: All passwords stored locally - never synced to the cloud
- **SQLCipher Encryption**: Military-grade AES-256 encryption for your database
- **Biometric Authentication**: Fingerprint and face unlock support
- **Security Analysis**: Built-in password strength analyzer
- **Auto-lock**: Configurable timeout for enhanced security

### 📱 User Experience
- **Material Design**: Clean, modern interface following Android design guidelines
- **Dark/Light Theme**: Customizable appearance with multiple theme options
- **Smart Organization**: Category-based password organization
- **Quick Actions**: One-tap copy, show/hide, and edit operations
- **Search & Filter**: Fast password search and sorting options

### 🔧 Advanced Features
- **Password Generator**: Create strong, unique passwords
- **Favorites**: Quick access to frequently used passwords
- **Recycle Bin**: Safely recover accidentally deleted passwords
- **URL Integration**: Store and open associated websites
- **Multi-Select**: Bulk operations for efficient management
- **Export/Import**: Backup and restore your password database

## 📱 Screenshots

| Home Screen | Add Password | Security Settings | Password Analysis |
|-------------|--------------|-------------------|-------------------|
| ![Home](screenshots/home.png) | ![Add](screenshots/add.png) | ![Security](screenshots/security.png) | ![Analysis](screenshots/analysis.png) |

## 🚀 Installation

### Requirements
- Android 7.0 (API level 24) or higher
- Biometric hardware (optional, for fingerprint/face unlock)

### Download
1. Download the latest APK from [Releases](https://github.com/Abhibth0/Most_Save/releases)
2. Enable "Install from unknown sources" in your device settings
3. Install the APK file

### Build from Source
```bash
# Clone the repository
git clone https://github.com/Abhibth0/Most_Save.git
cd Most_Save

# Build the project
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## 🏗️ Technical Architecture

### Built With
- **Language**: Java & Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room with SQLCipher encryption
- **UI**: Material Design Components
- **Security**: Android Security Crypto, Biometric API
- **Charts**: MPAndroidChart for password analytics

### Key Components
```
📁 app/src/main/java/com/example/mostsave/
├── 📁 data/
│   ├── 📁 database/     # Room database setup
│   ├── 📁 model/        # Data models (Password, Category)
│   ├── 📁 repository/   # Data access layer
│   └── 📁 security/     # Encryption utilities
├── 📁 ui/
│   ├── 📁 fragments/    # UI screens
│   ├── 📁 adapters/     # RecyclerView adapters
│   └── 📁 dialogs/      # Custom dialogs
├── 📁 utils/            # Utility classes
└── 📁 viewmodel/        # ViewModel classes
```

## 🔐 Security Features

### Encryption
- **Database**: SQLCipher with AES-256 encryption
- **Key Management**: Android Keystore for secure key storage
- **Password Hashing**: PBKDF2 with salt for master password

### Authentication
- **Biometric**: Fingerprint and face recognition
- **PIN/Pattern**: Fallback authentication methods
- **Auto-lock**: Configurable timeout (30 seconds to 30 minutes)

### Privacy
- **No Network**: Zero network permissions for core functionality
- **No Analytics**: No usage tracking or data collection
- **Local Storage**: All data remains on your device

## 📋 Usage Guide

### First Time Setup
1. **Install** the app and grant necessary permissions
2. **Set Master Password** or enable biometric authentication
3. **Import** existing passwords or start adding new ones
4. **Configure** categories and security settings

### Managing Passwords
- **Add**: Tap the floating action button (+) to add new passwords
- **Edit**: Long press or tap the edit icon on any password
- **Copy**: Tap the copy icon to copy username/password to clipboard
- **Delete**: Move unwanted passwords to recycle bin
- **Organize**: Use categories to group related passwords

### Security Best Practices
- Use the built-in password generator for strong passwords
- Enable biometric authentication for quick access
- Regularly analyze password strength using the analysis feature
- Keep the app updated for latest security patches

## 🛠️ Development

### Project Structure
```
MostSave/
├── app/
│   ├── src/main/
│   │   ├── java/           # Source code
│   │   ├── res/            # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts    # App-level build configuration
├── gradle/
│   └── libs.versions.toml  # Dependency versions
└── README.md
```

### Dependencies
- **Room**: Local database with encryption
- **Biometric**: Authentication API
- **Navigation**: Android Navigation Component
- **Material**: Material Design Components
- **MPAndroidChart**: Data visualization
- **Gson**: JSON serialization

### Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📊 Features Overview

| Feature | Description | Status |
|---------|-------------|--------|
| 🔐 Offline Storage | All data stored locally | ✅ |
| 🔒 Encryption | SQLCipher AES-256 | ✅ |
| 👆 Biometric Auth | Fingerprint/Face unlock | ✅ |
| 📱 Material Design | Modern Android UI | ✅ |
| 🌙 Dark Mode | Theme customization | ✅ |
| 📂 Categories | Password organization | ✅ |
| 🔍 Search | Fast password search | ✅ |
| 🗑️ Recycle Bin | Deleted password recovery | ✅ |
| 📊 Analytics | Password strength analysis | ✅ |
| 🔄 Backup/Restore | Data export/import | ✅ |
| 🔗 URL Integration | Website links | ✅ |
| ⭐ Favorites | Quick access | ✅ |

## 🔧 Configuration

### Security Settings
- **Master Password**: Set a strong master password
- **Biometric**: Enable fingerprint/face unlock
- **Auto-lock**: Configure timeout (30s - 30min)
- **Screen Security**: Prevent screenshots in recent apps

### Appearance
- **Theme**: Light, Dark, or System default
- **Colors**: Multiple color schemes available
- **Layout**: Compact or comfortable list view

## 🐛 Troubleshooting

### Common Issues
1. **Biometric not working**: Ensure biometric is set up in device settings
2. **App crashes**: Clear app data and reconfigure
3. **Forgotten master password**: No recovery possible - this is by design for security
4. **Database corruption**: Restore from backup if available

### Support
- Create an issue on GitHub for bug reports
- Check existing issues before creating new ones
- Provide device information and steps to reproduce

## 🔄 Version History

### v2.1.11 (Current)
- Enhanced security features
- Improved password analysis
- Bug fixes and performance improvements

### Previous Versions
- v2.1.x: Security enhancements
- v2.0.x: Major UI overhaul
- v1.x.x: Initial release

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Acknowledgments

- **Android Team** for excellent development tools
- **Material Design** for beautiful UI components
- **SQLCipher** for secure database encryption
- **Open Source Community** for various libraries used

## 📞 Contact

- **Developer**: Abhishek Patel
- **Email**: Abhishekpatelbth0@gmail.com
- **GitHub**: [github.com/Abhibth0](https://github.com/Abhibth0)

---

<div align="center">
  <strong>🔐 Your Security, Your Control 🔐</strong>
  <br>
  <sub>Made with ❤️ for privacy-conscious users</sub>
</div>
