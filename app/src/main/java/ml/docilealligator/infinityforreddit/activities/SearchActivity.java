package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.SearchActivityRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySearchBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQuery;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQueryViewModel;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends BaseActivity {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME = "ESISOUN";
    public static final String EXTRA_SEARCH_IN_MULTIREDDIT = "ESIM";
    public static final String EXTRA_SEARCH_IN_THING_TYPE = "ESITY";
    public static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    public static final String EXTRA_SEARCH_ONLY_USERS = "ESOU";
    public static final String EXTRA_SEARCH_SUBREDDITS_AND_USERS = "ESSAU";
    public static final String RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES = "RESSN";
    public static final String RETURN_EXTRA_SELECTED_USERNAMES = "RESU";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    public static final int SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE = 101;

    private static final String SEARCH_IN_SUBREDDIT_OR_NAME_STATE = "SNS";
    private static final String SEARCH_IN_THING_TYPE_STATE = "SITTS";
    private static final String SEARCH_IN_MULTIREDDIT_STATE = "SIMS";

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;
    private static final int USER_SEARCH_REQUEST_CODE = 2;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor executor;
    private String query;
    private String searchInSubredditOrUserName;
    private MultiReddit searchInMultiReddit;
    @SelectThingReturnKey.THING_TYPE
    private int searchInThingType;
    private boolean searchOnlySubreddits;
    private boolean searchOnlyUsers;
    private boolean searchSubredditsAndUsers;
    private SearchActivityRecyclerViewAdapter adapter;
    private SubredditAutocompleteRecyclerViewAdapter subredditAutocompleteRecyclerViewAdapter;
    private Handler handler;
    private Runnable autoCompleteRunnable;
    private Call<String> subredditAutocompleteCall;
    RecentSearchQueryViewModel mRecentSearchQueryViewModel;
    private ActivityResultLauncher<Intent> requestThingSelectionForCurrentActivityLauncher;
    private ActivityResultLauncher<Intent> requestThingSelectionForAnotherActivityLauncher;
    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        // DISABLED - Search blocked to keep locked to r/dumbphones
        android.widget.Toast.makeText(this, "Search is disabled", android.widget.Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSearchActivity, null, binding.toolbar);
        int toolbarPrimaryTextAndIconColorColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        binding.searchEditTextSearchActivity.setTextColor(toolbarPrimaryTextAndIconColorColor);
        binding.searchEditTextSearchActivity.setHintTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        binding.clearSearchEditViewSearchActivity.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.linkHandlerImageViewSearchActivity.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        int colorAccent = mCustomThemeWrapper.getColorAccent();
        binding.searchInTextViewSearchActivity.setTextColor(colorAccent);
        binding.subredditNameTextViewSearchActivity.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.deleteAllRecentSearchesButtonSearchActivity.setIconTint(ColorStateList.valueOf(mCustomThemeWrapper.getPrimaryIconColor()));
        binding.dividerSearchActivity.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), typeface);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.searchEditTextSearchActivity.requestFocus();

        if (query != null) {
            binding.searchEditTextSearchActivity.setText(query);
            binding.searchEditTextSearchActivity.setSelection(query.length());
            query = null;
        }

        Utils.showKeyboard(this, new Handler(), binding.searchEditTextSearchActivity);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, data.getStringArrayListExtra(SearchSubredditsResultActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES));
                } else {
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.SUBREDDIT);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == USER_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_USERNAMES, data.getStringArrayListExtra(SearchUsersResultActivity.RETURN_EXTRA_SELECTED_USERNAMES));
                } else {
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.USER);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE) {
                // Search is disabled
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_IN_SUBREDDIT_OR_NAME_STATE, searchInSubredditOrUserName);
        outState.putInt(SEARCH_IN_THING_TYPE_STATE, searchInThingType);
        outState.putParcelable(SEARCH_IN_MULTIREDDIT_STATE, searchInMultiReddit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}