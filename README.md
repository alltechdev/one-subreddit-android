# Single-Subreddit Reddit Client

A customizable Reddit client that locks to a single subreddit, perfect for focused browsing or creating dedicated apps for specific communities.

## What is this?

This is a modified version of Infinity for Reddit that restricts the app to browse only one subreddit. All navigation, search, and link functionality is disabled - users can only view posts from the configured subreddit.

**Current configuration:** Locked to `r/dumbphones`

## How to customize for your own subreddit

1. **Fork this repository**
   - Click the "Fork" button at the top of this page
   - Clone your fork: `git clone https://github.com/YOUR_USERNAME/Infinity-For-Reddit.git`

2. **Change the subreddit name in 3 files**

   **File 1:** `app/src/main/java/ml/docilealligator/infinityforreddit/activities/MainActivity.java`

   Find and replace all instances of `"dumbphones"` with your subreddit name (without the r/):
   - Line 360: `getSupportActionBar().setTitle("r/dumbphones");`
   - Line 1680: `bundle.putString(PostFragment.EXTRA_NAME, "dumbphones");`

   **File 2:** `app/src/main/java/ml/docilealligator/infinityforreddit/activities/ViewSubredditDetailActivity.java`

   - Line 219: `if (requestedSubreddit == null || !requestedSubreddit.equalsIgnoreCase("dumbphones")) {`

   **File 3:** `app/src/main/res/values/strings.xml`

   Change the app name (line 1):
   ```xml
   <string name="application_name" translatable="false">r/dumbphones</string>
   ```

## What's locked down?

- ✅ All feeds show only the configured subreddit
- ✅ Search completely disabled
- ✅ Cannot navigate to other subreddits
- ✅ Cannot view user profiles
- ✅ All links (internal & external) blocked
- ✅ Tab bar hidden (all tabs show same content)

## Modified files

The following files contain the subreddit locking logic:

- `app/src/main/java/ml/docilealligator/infinityforreddit/activities/MainActivity.java` - Main subreddit lock, UI changes
- `app/src/main/java/ml/docilealligator/infinityforreddit/activities/LinkResolverActivity.java` - Link blocking
- `app/src/main/java/ml/docilealligator/infinityforreddit/activities/ViewSubredditDetailActivity.java` - Subreddit access control
- `app/src/main/java/ml/docilealligator/infinityforreddit/activities/ViewUserDetailActivity.java` - User profile blocking
- `app/src/main/java/ml/docilealligator/infinityforreddit/activities/SearchActivity.java` - Search disable
- `app/src/main/res/menu/main_activity.xml` - Search icon removal
- `app/src/main/res/values/strings.xml` - App name

## License

Distributed under the AGPL-3.0 License. See [LICENSE](LICENSE) for more information.

## Original project

This is a fork of [Infinity for Reddit](https://github.com/Docile-Alligator/Infinity-For-Reddit) by [u/Hostilenemy](https://www.reddit.com/user/Hostilenemy).
