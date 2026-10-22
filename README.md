# PrimeCheck 🔢⚡

**PrimeCheck** is a modern, high-performance Android application built with Jetpack Compose and Kotlin that performs arbitrary-precision primality testing, prime factorization, factor pair generation, and batch mathematical analysis.

---

## 🌟 Key Features

### 1. 🔍 Single Number Primality & Analysis
- **Arbitrary Precision**: Test numbers of any size using Java/Kotlin's `BigInteger`.
- **Fast Primality Algorithms**:
  - Small primes trial division.
  - Deterministic **Miller-Rabin** primality test for numbers up to $3.317 \times 10^{24}$.
  - Probabilistic **Miller-Rabin** test (40 rounds) for ultra-large numbers.
  - **Pollard's Rho** algorithm for efficient non-trivial factor discovery.
- **Complete Factor Analysis**:
  - Prime Factorization with exponents (e.g. $360 = 2^3 \times 3^2 \times 5^1$).
  - Full factor listing & factor pairs for eligible bounds.
  - Total count of positive divisors calculation.
  - Perfect square detection and integer square root evaluation ($\lfloor\sqrt{N}\rfloor$).
- **Step-by-Step Explanation**: Detailed breakdown showing bounds, trial divisions, or Miller-Rabin witness details for educational transparency.

### 2. ⚡ Batch Processing
- Test lists or ranges of numbers simultaneously.
- Real-time progress updates, cancellable background execution, and filtering (Primes vs Composites).
- Export batch results.

### 3. 📜 History & Management
- Automatically record calculation history locally.
- Search, filter, copy, share, and delete past results.

### 4. 🎨 Modern Android UI & UX
- **Jetpack Compose & Material 3**: Beautiful, modern UI with Material You Dynamic Color support.
- **Dark & Light Modes**: Seamless switching or system sync.
- **Edge-to-Edge**: Full screen immersion with smooth animations.
- **Haptic Feedback & Share**: Custom tactile feedback and one-tap sharing of result summaries.

---

## 🛠️ Architecture & Tech Stack

- **Language**: 100% [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture**: MVVM (Model-View-ViewModel) with `StateFlow` and structured concurrency
- **Navigation**: Navigation Compose
- **Async & Threading**: Kotlin Coroutines & Flow
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`)
- **Testing**: JUnit & Coroutines Test Suite

---

## 📐 Mathematical Algorithms Overview

| Feature | Algorithm / Technique |
|---|---|
| **Primality Test** | Small Primes Trial Division $\rightarrow$ Deterministic Miller-Rabin (bases 2..41) $\rightarrow$ Probabilistic Miller-Rabin (40 rounds) |
| **Factorization** | Trial Division + Pollard's Rho with Brent's Cycle Detection |
| **Integer Square Root** | Newton's Method / Binary Search for BigInteger |
| **Verification** | Prime Check Validator composite witness verification |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 11 or higher
- Android SDK 37 (Min SDK 26 - Android 8.0 Oreo)

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/PrimeCheck.git
   cd PrimeCheck
   ```

2. **Open in Android Studio** and sync Gradle.

3. **Build & Run**:
   - Run via Android Studio onto an emulator or physical device.
   - Or build via command line:
     ```bash
     ./gradlew assembleDebug
     ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📁 Project Structure

```
PrimeCheck/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/primecheck/
│   │   │   │   ├── data/           # Repositories for History and Settings
│   │   │   │   ├── domain/math/    # PrimeChecker, PrimeFactorizer, MathUtils, etc.
│   │   │   │   ├── model/          # Data classes & UI States
│   │   │   │   ├── ui/             # Jetpack Compose Screens & ViewModel
│   │   │   │   └── util/           # Haptics, Sharing, and Helpers
│   │   │   └── res/                # Drawables, Strings, XML resources
│   │   └── test/                   # Unit test cases
│   └── build.gradle.kts
├── gradle/                         # Gradle wrapper & Version catalogs
├── build.gradle.kts
└── README.md
```

---

## 📄 License

This project is open-source under the MIT License. Feel free to fork, customize, and contribute!
