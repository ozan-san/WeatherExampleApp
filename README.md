# Weather Example App
---

![Weather app screenshot](/screenshots/thumbnail.png?raw=true)

An example weather app by Ozan Şan.

Features:

- **Location permission checks and location retrieval** via async Android & Kotlin APIs (Flow)
- **API-fed data for weather information**, provided by [Open-Meteo](https://open-meteo.com).
- **[OkHttp3](https://square.github.io/okhttp/) & [Retrofit](https://square.github.io/retrofit/)** for network requests.
- **[Gson](https://github.com/google/gson)** for response parsing / deserialization.
- **[Hilt](https://dagger.dev/hilt/)** for Dependency Injection. 
- **MVVM** architecture.
- **Unit-testable, Android-dependency-free ViewModel implementations.** JUnit & Mockito for testing & mocking.
- **Jetpack Compose** for UI implementation.
- **Immutable Collections (KotlinX)** for stable UI state where needed.
- **Dynamic fonts** from Google Fonts for on-demand font loading.