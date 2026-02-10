# 🎫 EventsAround

An Android app for discovering events near you, powered by the Ticketmaster Discovery API. Search for concerts, sports games, theatre shows, films, and more — all from one place.

## ✨ Features

- **Event Search** — Search events by keyword with real-time autocomplete suggestions
- **Location Awareness** — Auto-detect your current location or manually enter a location with Google Places autocomplete
- **Category Filtering** — Filter events by category: Music, Sports, Arts & Theatre, Film, Miscellaneous
- **Adjustable Search Radius** — Customize the search distance to find events near or far
- **Event Details** — View comprehensive event information including date, venue, pricing, ticket status, and seatmap
- **Artist Info** — For music events, view Spotify artist details including followers, popularity, genres, and albums
- **Venue Details** — Explore venue information fetched from the Ticketmaster API
- **Favorites** — Save your favorite events locally and access them from the home screen with real-time "time ago" display
- **Share & Buy Tickets** — Share event links and open Ticketmaster to purchase tickets directly
- **Dark Mode** — Full support for both light and dark themes with Material Design 3

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Navigation** | Navigation Compose |
| **Networking** | Retrofit + Gson Converter + OkHttp |
| **Async** | Kotlin Coroutines |
| **Image Loading** | Coil |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Local Storage** | SharedPreferences |
| **APIs** | Ticketmaster Discovery API, Spotify Web API, Google Places & Geocoding APIs |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |

## 🏗 Project Structure

```
com.csci571.hw4.eventsaround/
├── data/
│   ├── model/          # Data classes (Event, EventDetails, SearchParams, SpotifyArtist, etc.)
│   ├── remote/         # Retrofit ApiService, RetrofitClient, GooglePlacesService
│   └── repository/     # EventRepository — single source of truth for data
├── ui/
│   ├── components/     # Reusable composables (SearchBar, LocationSelector)
│   ├── navigation/     # Navigation graph & Screen sealed class
│   ├── screens/        # HomeScreen, SearchScreen, ResultsScreen, DetailScreen
│   ├── theme/          # Color, Theme, Typography (light & dark)
│   └── viewmodel/      # HomeViewModel, SearchViewModel, ResultsViewModel, EventDetailsViewModel
└── MainActivity.kt
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 21
- Android device or emulator running API 24+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/EventsAround.git
   cd EventsAround
   ```

2. **Configure API keys**

   Add your API keys to `local.properties` (or the project's `BuildConfig`):
   ```properties
   TICKETMASTER_API_KEY=your_ticketmaster_key
   GOOGLE_MAPS_API_KEY=your_google_key
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
   ```

3. **Set up the backend server**

   The app communicates with a Node.js backend that proxies API calls to Ticketmaster, Spotify, and Google services. Make sure the backend is running and update the base URL in `RetrofitClient` accordingly.

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open the project in Android Studio and click **Run ▶️**.

## 📱 Screens

### Home (Favorites)
Displays your favorited events with thumbnail, name, date, and a live "time ago" indicator. Tap an event to view its details. A "Powered by Ticketmaster" link is shown at the bottom.

### Search
Enter a keyword (with autocomplete), choose a location (auto-detect or manual entry with Google Places suggestions), set the search radius, and pick a category. Tap the search icon to find events.

### Results
Browse matching events displayed as cards with image, name, venue, date, and category. Filter results by category using the tab bar. Tap the star icon to add/remove favorites.

### Details
Three-tab detail view:
- **Details** — Event name, date, artists, venue, genres, price range, ticket status, seatmap, and links to buy tickets or share
- **Artist** — Spotify profile photo, followers, popularity, genres, and album discography (music events only)
- **Venue** — Venue information from Ticketmaster

## 📄 License

This project was developed as part of CSCI 571 (Web Technologies) coursework.

## 🙏 Acknowledgments

- [Ticketmaster Discovery API](https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/)
- [Spotify Web API](https://developer.spotify.com/documentation/web-api)
- [Google Places & Geocoding APIs](https://developers.google.com/maps/documentation)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Coil](https://coil-kt.github.io/coil/)
- [Retrofit](https://square.github.io/retrofit/)
