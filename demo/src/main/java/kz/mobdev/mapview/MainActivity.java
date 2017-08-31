package kz.mobdev.mapview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.onlylemi.mapview.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Class[] classes = {MapLayerTestActivity.class, BitmapLayerTestActivity.class,
            LocationLayerTestActivity.class, MarkerLayerTestActivity.class, RouteLayerTestActivity
            .class, ShapeLayerTestActivity.class};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.mapview_lv);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.maplayer_name));
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, classes[position].getSimpleName());
                startActivity(new Intent(MainActivity.this, classes[position]));
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
