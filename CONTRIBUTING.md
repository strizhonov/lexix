### Opening issues

Before opening an issue, please make sure that your issue:
- is not a duplicate (i.e. it has not been reported before, closed or open)
- has not been fixed
- is in English (issues in a language other than English will be closed unless someone translates them)
- does not contain multiple feature requests/bug reports. Please open a separate issue for each one.

### Code contribution

#### To get started with development:
1. [Clone](https://help.github.com/articles/cloning-a-repository/) the repository

2. Set up Yandex API
- Register at [yandex.com](https://yandex.com/dev/dictionary/keys/get/)
- Add `yandex_dictionary_api_key="<your_api_key>"` to your [`gradle.properties`](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) file.

3 Set up Unsplash API 
- Register at [unsplash.com](https://unsplash.com/developers)
- Add `unsplash_api_key="<your_api_key>"` to your [`gradle.properties`](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) file. You can create an access token or use your [default public token](https://docs.mapbox.com/help/glossary/access-token/#default-public-token)
