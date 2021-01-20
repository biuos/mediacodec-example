package dai.android.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dai.android.app.help.permission.Nammu;
import dai.android.app.help.permission.PermissionCallback;
import dai.android.debug.crash.CrashHandler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String ACTIVITY_CATEGORY = "dai.android.activity.category";
    private static final String INTENT_PATH = "dai.android.app.test.Path";

    private LinearLayoutManager layoutManager = null;
    private RecyclerView recyclerView = null;
    private ActivityAdapter activityAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CrashHandler.init(getApplicationContext());
        getPermission();

        recyclerView = findViewById(R.id.recyclerview);

        activityAdapter = new ActivityAdapter(this);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(activityAdapter);

        Intent intent = getIntent();
        String path = intent.getStringExtra(INTENT_PATH);
        if (path == null) {
            path = "";
        }

        activityAdapter.setData(getData(path));
    }


    protected List<Map<String, Object>> getData(String prefix) {
        List<Map<String, Object>> myData = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(ACTIVITY_CATEGORY);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        if (null == list) {
            return myData;
        }

        String[] prefixPath;
        String prefixWithSlash = prefix;

        if (prefix.equals("")) {
            prefixPath = null;
        } else {
            prefixPath = prefix.split("/");
            prefixWithSlash = prefix + "/";
        }

        int len = list.size();

        Map<String, Boolean> entries = new HashMap<>();

        for (int i = 0; i < len; i++) {
            ResolveInfo info = list.get(i);
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null ? labelSeq.toString() : info.activityInfo.name;
            if (prefixWithSlash.length() == 0 || label.startsWith(prefixWithSlash)) {

                String[] labelPath = label.split("/");
                String nextLabel = prefixPath == null ? labelPath[0] : labelPath[prefixPath.length];
                if ((prefixPath != null ? prefixPath.length : 0) == labelPath.length - 1) {
                    addItem(myData, nextLabel, activityIntent(
                            info.activityInfo.applicationInfo.packageName,
                            info.activityInfo.name));
                } else {
                    if (entries.get(nextLabel) == null) {
                        addItem(myData, nextLabel, browseIntent(prefix.equals("") ? nextLabel : prefix + "/" + nextLabel));
                        entries.put(nextLabel, true);
                    }
                }
            }
        }
        Collections.sort(myData, sDisplayNameComparator);

        return myData;
    }

    private final static Comparator<Map<String, Object>> sDisplayNameComparator =
            new Comparator<Map<String, Object>>() {
                private final Collator collator = Collator.getInstance();

                public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                    return collator.compare(map1.get("title"), map2.get("title"));
                }
            };

    protected Intent activityIntent(String pkg, String componentName) {
        Intent result = new Intent();
        result.setClassName(pkg, componentName);
        return result;
    }

    protected Intent browseIntent(String path) {
        Intent result = new Intent();
        result.setClass(this, MainActivity.class);
        result.putExtra(INTENT_PATH, path);
        return result;
    }

    protected void addItem(List<Map<String, Object>> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }


    private void getPermission() {
        Nammu.INSTANCE.init(getApplicationContext());
        askPermission(Manifest.permission.CAMERA);
        askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        askPermission(Manifest.permission.RECORD_AUDIO);
    }

    private void askPermission(String permissionName) {
        if (!Nammu.INSTANCE.checkPermission(permissionName)) {
            if (Nammu.INSTANCE.shouldShowRequestPermissionRationale(this, permissionName)) {
                Toast.makeText(this, R.string.permission_tips, Toast.LENGTH_SHORT).show();
                Nammu.INSTANCE.askForPermission(MainActivity.this, permissionName, new PermissionCallback() {
                    @Override
                    public void permissionGranted() {
                    }

                    @Override
                    public void permissionRefused() {
                    }
                });
            } else {
                Nammu.INSTANCE.askForPermission(MainActivity.this, permissionName, new PermissionCallback() {
                    @Override
                    public void permissionGranted() {
                    }

                    @Override
                    public void permissionRefused() {
                    }
                });
            }
        }
    }


    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    static class VH extends RecyclerView.ViewHolder {
        public TextView id;
        public TextView title;

        public VH(@NonNull View v) {
            super(v);

            id = v.findViewById(R.id.id);
            title = v.findViewById(R.id.title);
        }
    }

    static class ActivityAdapter extends RecyclerView.Adapter<VH> {

        private final Activity activity;

        ActivityAdapter(Activity _activity) {
            activity = _activity;
        }

        private List<Map<String, Object>> values = null;

        public void setData(List<Map<String, Object>> data) {
            values = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.id.setText(Integer.toString(position));
            if (null != values && !values.isEmpty()) {
                Map<String, Object> item = values.get(position);
                String title = (String) item.get("title");
                if (!TextUtils.isEmpty(title)) {
                    holder.title.setText(title);

                    Intent intent = (Intent) item.get("intent");
                    if (null != intent) {
                        holder.itemView.setOnClickListener(v -> activity.startActivity(intent));
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            if (null == values || values.isEmpty())
                return 0;

            return values.size();
        }
    }
}