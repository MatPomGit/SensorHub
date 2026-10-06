# 🔨 Build Instructions

## Quick Build Guide

### Prerequisites Check
```bash
# Check Java version (need JDK 17+)
java -version

# Check Android SDK
echo $ANDROID_HOME

# Check Gradle
./gradlew --version
```

### Step-by-Step Build

#### 1. Extract Archive
```bash
tar -xzf SensorHub-Ultimate.tar.gz
cd SensorHub
```

#### 2. Verify Project Structure
```bash
ls -la
# Should see: app/, gradle/, build.gradle.kts, settings.gradle.kts
```

#### 3. Clean Build
```bash
./gradlew clean
```

#### 4. Build Debug APK
```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### 5. Install on Device
```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or via Gradle
./gradlew installDebug
```

---

## Using Android Studio

### Import Project
1. **Open Android Studio**
2. **File → Open**
3. **Navigate to SensorHub directory**
4. **Click OK**
5. **Wait for Gradle Sync** (may take 2-5 minutes first time)

### Build in IDE
1. **Build → Make Project** (Ctrl+F9 / Cmd+F9)
2. **Wait for build to complete**
3. **Check Build Output** window for errors

### Run on Device
1. **Connect Android device** via USB
2. **Enable USB Debugging** on device
3. **Select device** in dropdown
4. **Click Run** ▶️ button
5. **App launches** on device

---

## Troubleshooting Build Issues

### Gradle Sync Failed

**Problem**: "Failed to sync Gradle project"

**Solutions**:
```bash
# 1. Invalidate caches
File → Invalidate Caches → Invalidate and Restart

# 2. Re-download dependencies
./gradlew clean --refresh-dependencies

# 3. Check internet connection
ping google.com
```

### JDK Version Mismatch

**Problem**: "Unsupported Java version"

**Solution**:
```bash
# Set JAVA_HOME to JDK 17
export JAVA_HOME=/path/to/jdk-17

# Verify
java -version
```

### Android SDK Not Found

**Problem**: "SDK location not found"

**Solution**:
1. Create `local.properties`:
```properties
sdk.dir=/path/to/Android/sdk
```

2. Or set environment:
```bash
export ANDROID_HOME=/path/to/Android/sdk
```

### Build Too Slow

**Problem**: Gradle build takes forever

**Solution**:
Add to `gradle.properties`:
```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError
```

### KSP Errors

**Problem**: "KSP annotation processing failed"

**Solution**:
```bash
# Clean KSP cache
./gradlew cleanBuildCache

# Rebuild
./gradlew build --no-build-cache
```

### Dependency Resolution Failed

**Problem**: "Could not resolve dependency"

**Solution**:
```bash
# 1. Clear Gradle cache
rm -rf ~/.gradle/caches/

# 2. Sync again
./gradlew build --refresh-dependencies
```

---

## Build Variants

### Debug (Default)
```bash
./gradlew assembleDebug
```
- Debuggable
- Logging enabled
- No ProGuard
- Uses debug signing

### Release
```bash
./gradlew assembleRelease
```
- Not debuggable
- Logging disabled
- ProGuard enabled (minified)
- Requires release signing

### Create Signed APK

1. **Generate Keystore**:
```bash
keytool -genkey -v -keystore sensorhub.keystore \
  -alias sensorhub -keyalg RSA -keysize 2048 -validity 10000
```

2. **Create `keystore.properties`**:
```properties
storeFile=../sensorhub.keystore
storePassword=your_password
keyAlias=sensorhub
keyPassword=your_password
```

3. **Build Signed APK**:
```bash
./gradlew assembleRelease
```

---

## Build Optimization

### Speed Up Builds

**1. Enable Gradle Daemon**
```properties
# gradle.properties
org.gradle.daemon=true
org.gradle.configureondemand=true
```

**2. Increase Heap Size**
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

**3. Enable Parallel Execution**
```properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

**4. Enable Build Cache**
```properties
org.gradle.caching=true
android.enableBuildCache=true
```

### Reduce APK Size

**1. Enable ProGuard**
```kotlin
// build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
    }
}
```

**2. Use APK Splits**
```kotlin
splits {
    abi {
        isEnable = true
        reset()
        include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        isUniversalApk = false
    }
}
```

---

## CI/CD Setup

### GitHub Actions

Aktualny workflow `.github/workflows/android-ci.yml` przechowuje debug APK i raporty tylko krótkoterminowo. Ręczne uruchomienie może opcjonalnie utworzyć trwały techniczny GitHub prerelease (`publish_prerelease=true` + `release_tag`) po przejściu testów, lint i testów instrumentacyjnych. Release zawiera APK i sumę SHA-256; nie zastępuje produkcyjnego podpisanego wydania.

Poniższy fragment jest jedynie przykładem alternatywnej konfiguracji CI:

Create `.github/workflows/build.yml`:
```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Build with Gradle
      run: ./gradlew build
    - name: Run tests
      run: ./gradlew test
```

### GitLab CI

Create `.gitlab-ci.yml`:
```yaml
image: openjdk:17-jdk

before_script:
  - export ANDROID_HOME=$PWD/android-sdk
  - chmod +x ./gradlew

build:
  stage: build
  script:
    - ./gradlew assembleDebug
  artifacts:
    paths:
      - app/build/outputs/apk/debug/

test:
  stage: test
  script:
    - ./gradlew test
```

---

## Verification

### Check APK
```bash
# List APK contents
unzip -l app/build/outputs/apk/debug/app-debug.apk

# Check APK size
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Analyze APK
./gradlew analyzeDebugApk
```

### Run Lint
```bash
./gradlew lint

# View report
open app/build/reports/lint-results.html
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Android tests (requires device/emulator)
./gradlew connectedAndroidTest
```

---

## Common Commands

```bash
# Clean
./gradlew clean

# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Uninstall
./gradlew uninstallDebug

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Generate docs
./gradlew dokkaHtml

# List tasks
./gradlew tasks

# Dependency tree
./gradlew dependencies
```

---

## Success Indicators

✅ **Build Successful**
```
BUILD SUCCESSFUL in 45s
42 actionable tasks: 42 executed
```

✅ **APK Generated**
```
app/build/outputs/apk/debug/app-debug.apk (8.5 MB)
```

✅ **Tests Passed**
```
Test Summary:
20 tests completed, 20 passed
```

---

## Need Help?

### Resources
- 📖 [Android Studio User Guide](https://developer.android.com/studio/intro)
- 🔧 [Gradle Documentation](https://docs.gradle.org)
- 🐛 [Troubleshooting](CHANGELOG_AND_ISSUES.md#troubleshooting)

### Support
- Open an [Issue](https://github.com/yourusername/SensorHub/issues)
- Check [Known Issues](CHANGELOG_AND_ISSUES.md#known-issues)
- Email: dev@sensorhub.dev

---

**Happy Building! 🚀**
