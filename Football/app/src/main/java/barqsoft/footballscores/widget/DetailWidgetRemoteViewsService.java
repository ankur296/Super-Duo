package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private String[] fragmentdate = new String[1];
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public double detail_match_id = 0;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            public  final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                //calc today's date
                String[] fragmentdate = new String[1];
                Date todaysDate = new Date(System.currentTimeMillis() + 0*86400000);
                SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd");
                fragmentdate[0] = mSimpleDateFormat.format(todaysDate);
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        null,null,fragmentdate,null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String home = data.getString(COL_HOME);
                String away = data.getString(COL_AWAY);
                String matchTime = data.getString(COL_MATCHTIME);
                String score = Utilies.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
                double matchId = data.getDouble(COL_ID);
                int homeImg = Utilies.getTeamCrestByTeamName(data.getString(COL_HOME));
                int awayImg = Utilies.getTeamCrestByTeamName(data.getString(COL_AWAY));

                // Add the data to the RemoteViews
                views.setImageViewResource(R.id.home_crest, homeImg);
                views.setImageViewResource(R.id.away_crest, awayImg);
                views.setTextViewText(R.id.home_name, home);
                views.setTextViewText(R.id.away_name, away);
                views.setTextViewText(R.id.data_textview, matchTime);
                views.setTextViewText(R.id.score_textview, score);

                final Intent fillInIntent = new Intent();
//                fillInIntent.setData(weatherUri); //TODO: set the data with which app shud be
                //TODO:launched when collections widget is clicked..in this case set the list item clicked..
                //TODO:so that that particular list item is expanded on app launch
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
