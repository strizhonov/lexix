### Opening issues

Before opening an issue, please make sure that your issue:
- is not a duplicate (i.e. it has not been reported before, closed or open)
- has not been fixed
- is in English (issues in a language other than English will be closed unless someone translates them)
- does not contain multiple feature requests/bug reports. Please open a separate issue for each one.

### Code contribution

#### To get started with development:
1. [Fork](https://help.github.com/articles/fork-a-repo/) and [clone](https://help.github.com/articles/cloning-a-repository/) the repository
2. Install and launch [Android Studio](https://developer.android.com/studio)
3. Select `File > Open`, select the Tasks directory, and accept prompts to install missing SDK components

#### Set up Yandex API
1. Register at [yandex.com](https://yandex.com/dev/dictionary/keys/get/)
2. Add `yandex_dictionary_api_key="<your_api_key>"` to your [`gradle.properties`](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) file.

#### Set up Unsplash API
1. Register at [unsplash.com](https://unsplash.com/developers)
2. Add `unsplash_api_key="<your_api_key>"` to your [`gradle.properties`](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) file. You can create an access token or use your [default public token](https://docs.mapbox.com/help/glossary/access-token/#default-public-token)
