package net.cdmsoftware.guardiannews;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    private static final String API_REQUEST_URL = "http://content.guardianapis.com/search";

    private NewsAdapter adapter;
    private TextView emptyView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<News> newsList = new ArrayList<>();
        adapter = new NewsAdapter(this, newsList);

        ListView newsListView = (ListView) findViewById(R.id.list);
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String url = newsList.get(i).getWebUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // Progress Bar view
        progressBar = (ProgressBar) findViewById(R.id.progress);

        // View for storing empty state info
        emptyView = (TextView) findViewById(R.id.emptyView);

        // assign empty view for listview
        newsListView.setEmptyView(emptyView);
        newsListView.setAdapter(adapter);

        if (hasInternetConnection()) {
            getLoaderManager().initLoader(1, null, this).forceLoad();
        } else {
            progressBar.setVisibility(View.GONE);
            emptyView.setText(R.string.no_internet_connection);
        }
    }

    private boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri = Uri.parse(API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        progressBar.setVisibility(View.VISIBLE);

        // default query if none specified
        String query = "android";
        if (bundle != null) {
            query = bundle.getString("q");
        }


        uriBuilder.appendQueryParameter("q", query);
        uriBuilder.appendQueryParameter("from-date", "2016-01-01");
        uriBuilder.appendQueryParameter("api-key", "test");
        uriBuilder.appendQueryParameter("show-fields", "byline");
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newsList) {
        progressBar.setVisibility(View.GONE);
        adapter.clear();
        if (!newsList.isEmpty()) {
            adapter.addAll(newsList);
        } else {
            emptyView.setText(R.string.no_data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (hasInternetConnection()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("q", query);
                    getLoaderManager().restartLoader(1, bundle, MainActivity.this).forceLoad();
                    return true;
                } else {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setText(R.string.no_internet_connection);
                    Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
