package com.fx.app.fxmplayer;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fx.app.sqlite.DB;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , MediaPlayer.OnPreparedListener{


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rcvw_track = findViewById(R.id.rcvw_track);
        rcvw_track.setAdapter(mAdapterTrack = new Adapter());

        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        txt_title = findViewById(R.id.txt_title);
        btn_play_pause = findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(this);

        seek_total = findViewById(R.id.seek_total);
        seek_line = findViewById(R.id.seek_line);
        seek_start = findViewById(R.id.seek_start);
        seek_end   = findViewById(R.id.seek_end);

        mOnNavigationItemSelectedListener
        = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            return true;
                        case R.id.navigation_dashboard:
                            return true;
                        case R.id.navigation_notifications:
                            addFile();
                            return true;
                    }
                } catch (Exception e) {
                    Log.e("", "", e);
                }
                return false;
            }
        };
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        /*
         *
         */
        openLast();
    }

    @Override
    public void onClick(View view) {
        try {
            if (view == btn_add) {
                addTrack();
            }
            else if (view == btn_play_pause) {
                if (btn_play_pause.isActivated()) {
                    mPlayer.pause();
                    mSeekRunnable = null;
                }
                else {
                    mPlayer.start();
                    initializeSeek();
                }

                btn_play_pause.setActivated(!btn_play_pause.isActivated());
            }
        } catch (Exception e) {
            Log.e("", "", e);
        }

    }

    private static final int REQUEST_CODE_ADD_FILE = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case REQUEST_CODE_ADD_FILE:
                    onActivityResultAddFile(requestCode, resultCode, data);
                    break;

            }

        } catch (Exception e) {
            Log.e("", "", e);
        }
    }

    private int mFileId = -1;
    private void openLast() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!App.isServiceReady) {
                        Thread.sleep(500);
                    }
                    Cursor cr = DB.query("select path, _id from FILE order by last_update desc limit 1");
                    if (cr.moveToFirst()) {
                        prepare(new File(cr.getString(0)));
                        mFileId = cr.getInt(1);
                    }
                    cr.close();

                    mAdapterTrack.DATA.clear();
                    cr = DB.query("select _id, title, sc_time, ec_time from FILE_TRACK order by sq_no desc limit 1");
                    while (cr.moveToNext()) {
                        mAdapterTrack.DATA.add(new Object[]{cr.getInt(0), cr.getString(1), cr.getInt(2), cr.getInt(3)});
                    }
                    rcvw_track.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapterTrack.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void refreshTracks() {
        new Thread() {
            @Override
            public void run() {
                try {

                    mAdapterTrack.DATA.clear();
                    Cursor cr = DB.query("select _id, title, sc_time, ec_time from FILE_TRACK order by sq_no desc limit 1");
                    while (cr.moveToNext()) {
                        }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private Adapter mAdapterTrack;


    private MediaMetadataRetriever mInfo;
    private MediaPlayer mPlayer;
    private Button btn_add;
    private TextView txt_title;
    private ImageButton btn_play_pause;
    private RecyclerView rcvw_track;

    private void prepare(File file) throws Exception {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        mInfo= new MediaMetadataRetriever();
        mInfo.setDataSource(file.getAbsolutePath());

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(MainActivity.this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDataSource(getApplicationContext(), Uri.fromFile(file));
        mPlayer.prepare();
    }


    private TextView seek_start;
    private TextView seek_end;
    private View seek_total;
    private View seek_line;
    private Runnable mSeekRunnable;
    private int[] mSeekSelection = new int[]{0, 0};
    private void initializeSeek() {
        if (mSeekRunnable != null) {
            return;
        }
        mSeekRunnable = new Runnable() {
            @Override
            public void run() {
                /*
                 * SEEK, TIMER, DLL
                 */
                try {
                    ViewGroup.LayoutParams params = seek_line.getLayoutParams();
                    params.width = seek_total.getWidth() * mPlayer.getCurrentPosition() / mPlayer.getDuration();
                    seek_line.setLayoutParams(params);

                    seek_start.setText(Util.toDisplay(mPlayer.getCurrentPosition()));
                    seek_end  .setText(Util.toDisplay(mPlayer.getDuration()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mSeekRunnable != null) {
                    Handler handler = seek_total.getHandler();
                    if (handler != null) {
                        handler.postDelayed(mSeekRunnable, 1000);
                    }
                }
            }
        };
        Handler handler = seek_total.getHandler();
        if (handler != null) {
            handler.postDelayed(mSeekRunnable, 1000);
        }
    }


    private void addTrack() {
        View view = View.inflate(MainActivity.this, R.layout.track_add_dialog, null);
        final EditText edtx_title = view.findViewById(R.id.edtx_title);
        final EditText edtx_start = view.findViewById(R.id.edtx_start);
        final EditText edtx_end   = view.findViewById(R.id.edtx_end);
        Button   btn_save   = view.findViewById(R.id.btn_save);
        CheckBox chbx_seek  = view.findViewById(R.id.chbx_selection);

        chbx_seek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        edtx_start.setEnabled(false);
                        edtx_end  .setEnabled(false);
                        edtx_start.setText(Util.toDisplay(mSeekSelection[0]));
                        edtx_end  .setText(Util.toDisplay(mSeekSelection[1]));
                    }
                    else {
                        edtx_start.setEnabled(true);
                        edtx_end  .setEnabled(true);
                    }
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }
        });
        chbx_seek.setChecked(true);


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addTrack(edtx_title.getText().toString(), edtx_start.getText().toString(), edtx_end.getText().toString());
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }
        });

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void addTrack(final String p_title, final String p_start, final String p_end) {
        new AsyncTask<String, String, Object[]>() {
            @Override
            protected Object[] doInBackground(String... strings) {

                int sq_no = 1;
                Cursor cursor = DB.query("select sq_no from FILE_TRACK where file_id=" + mFileId + " order by sq_no desc limit 1");
                if (cursor.moveToFirst()) {
                    sq_no = cursor.getInt(0) + 1;
                }

                int sc=0, ec=0;

                ContentValues cvalues = new ContentValues();
                cvalues.put("file_id", mFileId);
                cvalues.put("title", p_title);
                cvalues.put("sq_no", sq_no);
                cvalues.put("sc_time", sc = Util.fromDisplay(p_start));
                cvalues.put("ec_time", ec = Util.fromDisplay(p_end));
                long id = DB.insert("FILE_TRACK", cvalues);

                return new Object[]{Long.valueOf(id).intValue(), p_title, sc, ec};
            }

            @Override
            protected void onPostExecute(Object[] r) {
                mAdapterTrack.DATA.add(r);
                mAdapterTrack.notifyItemInserted(mAdapterTrack.DATA.size() - 1);
            }
        }.execute();


//        db.execSQL("CREATE TABLE FILE_TRACK(" +
//                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
//                "title TEXT NOT NULL," +
//                "sq_no INTEGER NOT NULL," +
//                "sc_time TEXT NOT NULL," +
//                "ec_time TEXT NOT NULL" +
//                ")");
    }

    private void addFile() throws Exception {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,REQUEST_CODE_ADD_FILE);
    }
    private void onActivityResultAddFile(int requestCode, int resultCode, final Intent data) {
        if(resultCode == RESULT_OK) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        String path = FileUtil.getPath(MainActivity.this, data.getData());

                        ContentValues cvalues = new ContentValues();
                        cvalues.put("path", path);
                        cvalues.put("last_update", System.currentTimeMillis());

                        DB.insert("FILE", cvalues);

                        File file = new File(path);
                        if (!file.exists()) {
                            System.out.println("FILE NOT FOUND!!!");
                          return;
                        }

                        prepare(file);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        txt_title.setText(mInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + " " + mInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
    }

    private class Holder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView time;

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        public ArrayList<Object[]> DATA = new ArrayList<>();
        //0:_id 1:title 2:sc 3:ec

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = viewGroup.inflate(viewGroup.getContext(), R.layout.track_adapter, null);
            Holder holder = new Holder(view);
            holder.title  = view.findViewById(R.id.title);
            holder.time   = view.findViewById(R.id.time);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int index) {
            Object[] data = DATA.get(index);
            holder.title.setText((String) data[1]);
            holder.time .setText(Util.toDisplay((int) data[2]) + " - " + Util.toDisplay((int) data[3]) );
        }

        @Override
        public int getItemCount() {
            return DATA.size();
        }
    }


}
