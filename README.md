# Twitter Counter

Jetpack Compose sample that mirrors Twitter's character counting rules and posts tweets through the Twitter v2 API. Includes authentication, real-time metrics, grammar checking, and a UI that follows the provided design reference.

## What you get
- Tweet composer with live weighted character counting (TwitterText rules, 280 limit).
- Validation and grammar/spell feedback (LanguageTool API).
- OAuth2 PKCE sign-in and tweet publishing via Twitter v2.
- Feedback effects: toasts, copy-to-clipboard, post state transitions.
- Modular layers (domain, data, presentation) with Koin DI.
- Unit tests for use cases, ViewModel, network error mapping, and repository behaviors (bonus).


## Demo video
https://github.com/user-attachments/assets/bd161f8f-8e07-460a-bf6a-4363e5f15de2
- Note: The included video will show the flow without posting to X because free developer accounts hit rate limits for tweet creation. The app will still exercise UI, validation, and error handling paths.

## Project structure
- `app/src/main/java/com/moe/twitter/presentation/twitter`: Compose UI and `TwitterViewModel`.
- `app/src/main/java/com/moe/twitter/domain`: Use cases, models, constants.
- `app/src/main/java/com/moe/twitter/data`: Repositories, Retrofit services, interceptors, auth helpers.
- `app/src/test/java/com/moe/twitter`: Unit tests for domain, presentation, and data layers.

## Setup
1) Prereqs: Android Studio Jellyfish+ (AGP 8.x), JDK 11.
2) In `local.properties`, add your Twitter and LanguageTool settings:
```
TWITTER_CLIENT_ID=your_client_id
TWITTER_REDIRECT_URI=your_redirect_uri
TWITTER_BEARER_TOKEN=your_bearer_token
```
3) Sync the project and run on an emulator or device (minSdk 26).

## Build & run
- Debug build: `./gradlew assembleDebug`
- Run app from Android Studio: select `app` → Run.

## Tests (bonus)
- Run unit tests: `./gradlew test`
- Covered areas:
  - Character metrics computation (`ComputeTweetMetricsUseCase`)
  - Text issue checking (`CheckTextIssuesUseCase`)
  - Tweet posting use case (`PostTweetUseCase`)
  - ViewModel flows and effects (`TwitterViewModelTest`)
  - Network error mapping (`NetworkErrorMapperTest`)
  - Repository behaviors for posting/metrics (`TweetRepositoryImplTest`)

## Notes for reviewers
- Character limits match TwitterText weighted length, not naive char counts.
- Posting handles API failures, duplicate tweet errors, offline/timeout cases, and idle reset delays.
- The tweet composer UI lives in a dedicated package and can be embedded elsewhere.
- Provide valid Twitter credentials to exercise the posting flow; otherwise, use tests to verify logic without hitting the network.

## Submission
Push the repo to GitHub and share the link per the challenge instructions.

