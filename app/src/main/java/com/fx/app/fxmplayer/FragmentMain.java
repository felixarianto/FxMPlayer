package com.fx.app.fxmplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fx.app.sqlite.DB;
import com.taishi.library.Indicator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentMain extends FragmentView implements View.OnClickListener , MediaPlayer.OnPreparedListener {

    private final String TAG = "FragmentMain";

    public FragmentMain() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mPlayer != null) {
                mPlayer.stop();
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    public void onCreateView(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.fragment_main);
        initButton();
        /*
         * INFO
         */
        txt_title    = findViewById(R.id.txt_title);
        txt_subtitle = findViewById(R.id.txt_subtitle);
        txt_durations= findViewById(R.id.txt_durations);
        txt_section_count = findViewById(R.id.txt_section_count);
        img_album         = findViewById(R.id.img_album);

        /*
         * LIST
         *
         */
        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        lyt_nosection = findViewById(R.id.lyt_nosection);
        rcvw_track    = findViewById(R.id.rcvw_track);
        rcvw_track.setAdapter(mAdapterTrack = new Adapter());
        rcvw_track_layout_manager = (LinearLayoutManager) rcvw_track.getLayoutManager();

        RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                try {
                    if (mAdapterTrack.DATA.isEmpty()) {
                        lyt_nosection.setVisibility(View.VISIBLE);
                    }
                    else {
                        lyt_nosection.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "AdapterDataObserver", e);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                onChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                onChanged();
            }

        };

        mAdapterTrack.registerAdapterDataObserver(observer);

        btn_play_pause = findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(this);
        btn_next_track     = findViewById(R.id.btn_next_track);
        btn_next_track.setOnClickListener(this);
        btn_previous_track = findViewById(R.id.btn_previous_track);
        btn_previous_track.setOnClickListener(this);

        /*
         * SEEKER
         *
         */
        track_selected = findViewById(R.id.track_selected);
        seek_total = findViewById(R.id.seek_total);
        seek_start = findViewById(R.id.seek_start);
        seek_end   = findViewById(R.id.seek_end);
        seek_bar   = findViewById(R.id.seek_bar);
        seek_bar.setListener(new SeekBar.Listener() {

            @Override
            public void onChangedSc(int value) {
                try {
                    seek_start.setText(Util.toDisplay(value));
                    seek_bar.setValue(value);
                    mPlayer .seekTo(value);
                    clearSelection();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChangedEc(int value) {
                try {
                    seek_end  .setText(Util.toDisplay(value));
                    if (value < mPlayer.getCurrentPosition()) {
                        mPlayer.seekTo(seek_bar.getSc());
                    }
                    clearSelection();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        /*
         *
         */
        openLast();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.open) {

        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        try {
            if (view == btn_add) {
                AdsUtil.showInterstitial(new Runnable() {
                    @Override
                    public void run() {
                        addTrack();
                    }
                });
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

                if (mSelectedTrack != null) {
                    mAdapterTrack.notifyItemChanged(mAdapterTrack.DATA.indexOf(mSelectedTrack));
                }

            }
            else if (view == btn_next_track) {
                nextTrack();
            }
            else if (view == btn_previous_track) {
                prevTrack();
            }
        } catch (Exception e) {
            Log.e("", "", e);
        }
    }


    public static final int REQUEST_CODE_ADD_FILE = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Log.w(TAG, "onActivityResult");
            switch (requestCode) {
                case REQUEST_CODE_ADD_FILE:
                    onActivityResultAddFileFromLibrary(requestCode, resultCode, data);
                    break;

            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void initButton() {
        findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_FILE);
                getActivity().overridePendingTransition(R.anim.translate_right_to_left_enter, R.anim.translate_right_to_left_exit);
            }
        });
    }

    private FileHolder mFileHolder;
    private void openLast() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Cursor cr = DB.query("select _id from FILE order by last_update desc limit 1");
                    if (!cr.moveToFirst()) {
                        cr.close();
                        Log.w(TAG, "No Last Item");
                        mFileHolder = new FileHolder();
                        mFileHolder.path = "raw";
                        prepare();
                        return;
                    }
                    int _id = cr.getInt(0);

                    mFileHolder = FileHolder.find(getActivity(), _id);
                    if (mFileHolder == null) {
                        Log.w(TAG, "Cant Find Item " + _id);
                        return;
                    }
                    prepare();
                } catch (Exception e) {
                    Log.e(TAG, "openLast", e);
                }
            }
        }.start();
    }


    private Adapter mAdapterTrack;
    private MediaMetadataRetriever mAudioInfo;
    private MediaPlayer mPlayer;
    private ImageButton btn_add;
    private ImageButton btn_next_track;
    private ImageButton btn_previous_track;
    private ImageView img_album;
    private TextView txt_title;
    private TextView txt_subtitle;
    private TextView txt_durations;
    private TextView txt_section_count;

    private ImageButton btn_play_pause;
    private RecyclerView rcvw_track;
    private View lyt_nosection;
    private LinearLayoutManager rcvw_track_layout_manager;
    private Object[] mSelectedTrack = null;

    private void prepare() throws Exception {
        if (mFileHolder == null) {
            return;
        }

        File file = null;
        if (mFileHolder.path.equals("raw")) {
            File directory = new File(Environment.getExternalStorageDirectory(),"Audio");
            if (!directory.exists()) {
                directory.mkdir();
            }
            if (directory.listFiles().length == 0) {
                InputStream iStream = getActivity().getResources().openRawResource(R.raw.al_fatihah_muzammil);
                ByteArrayOutputStream byteStream = null;
                byte[] buffer = new byte[iStream.available()];
                iStream.read(buffer);

                byteStream = new ByteArrayOutputStream();
                byteStream.write(buffer);
                byteStream.close();
                iStream.close();

                file = new File(directory,"Al-Fatihah.mp3");
                file.createNewFile();

                FileOutputStream output = new FileOutputStream(file);
                output.write(buffer);
                output.flush();
            }

            if (directory.listFiles().length == 0) {
                return;
            }
            file = directory.listFiles()[0];
            if (!file.isFile() || !file.exists()) {
                return;
            }
        }
        else {
            file = new File(mFileHolder.path);
            if (!file.isFile() || !file.exists()) {
                return;
            }
        }


        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        mAudioInfo = new MediaMetadataRetriever();
        mAudioInfo.setDataSource(file.getAbsolutePath());

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(FragmentMain.this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDataSource(getActivity(), Uri.fromFile(file));
        mPlayer.prepare();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        seek_bar .setMaxValue(mPlayer.getDuration());
        String title  = mAudioInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = mAudioInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        txt_title   .setText(title == null ? "" : title);
        txt_subtitle.setText(artist == null ? "" : artist);
        txt_durations.setText(Util.toDisplay(mPlayer.getDuration()));
        track_selected.setText("");

        Glide.with(img_album).load(mFileHolder.album_art).into(img_album);



        new Thread() {
            @Override
            public void run() {
                try {
                    mSelectedTrack = null;
                    mAdapterTrack.DATA.clear();

                    Cursor cr = DB.query("select _id, title, sc_time, ec_time from FILE_TRACK where file_id=" + mFileHolder._id + " order by sq_no");
                    while (cr.moveToNext()) {
                        mAdapterTrack.DATA.add(new Object[]{cr.getInt(0), cr.getString(1), cr.getInt(2), cr.getInt(3)});
                    }
                    cr.close();
                    if (rcvw_track.getHandler() != null) {
                        rcvw_track.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapterTrack    .notifyDataSetChanged();
                                txt_section_count.setText(mAdapterTrack.DATA.size() + " Sections");
                                lyt_nosection    .setVisibility(mAdapterTrack.DATA.isEmpty() ? View.VISIBLE : View.INVISIBLE);
                                seekTo(0, mPlayer.getDuration());
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }.start();
    }


    private TextView seek_start;
    private TextView seek_end;
    private View seek_total;
    private SeekBar  seek_bar;

    private Runnable mSeekRunnable;
    private TextView track_selected;

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
                    int delay = 500;

                    seek_bar  .setValue(mPlayer.getCurrentPosition() + delay);
                    seek_start.setText(Util.toDisplay(mPlayer.getCurrentPosition()));

                    if (mPlayer.getCurrentPosition() >= seek_bar.getEc()) {
                        mPlayer.seekTo(seek_bar.getSc());//REPEAT
                        if (btn_play_pause.isActivated() && !mPlayer.isPlaying()) {
                            mPlayer.start();
                        }
                    }

                    if (mSeekRunnable != null) {
                        Handler handler = seek_total.getHandler();
                        if (handler != null) {
                            handler.postDelayed(mSeekRunnable, delay);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "mSeekRunnable", e);
                }
            }
        };
        Handler handler = seek_total.getHandler();
        if (handler != null) {
            handler.postDelayed(mSeekRunnable, 1000);
        }
    }

    private void seekTo(int sc, int ec) {
        seek_start.setText(Util.toDisplay(sc));
        seek_end  .setText(Util.toDisplay(ec));

        seek_bar.setMaxValue(ec);
        seek_bar.setSc(sc);
        seek_bar.setEc(ec);
        seek_bar.setValue(sc);
        mPlayer.seekTo(sc);
    }



    private void addTrack() {
        final Dialog dialog = new Dialog(getActivity());
        View view = View.inflate(getActivity(), R.layout.track_add_dialog, null);
        final EditText edtx_title = view.findViewById(R.id.edtx_title);
        final EditText edtx_sc_0 = view.findViewById(R.id.edt_sc_0);
        final EditText edtx_sc_1 = view.findViewById(R.id.edt_sc_1);
        final EditText edtx_ec_0 = view.findViewById(R.id.edt_ec_0);
        final EditText edtx_ec_1 = view.findViewById(R.id.edt_ec_1);
        Button   btn_save   = view.findViewById(R.id.btn_save);
        ImageButton   btn_cancel = view.findViewById(R.id.btn_cancel);
        CheckBox chbx_seek  = view.findViewById(R.id.chbx_selection);

        edtx_title.setText(mAdapterTrack.getNewTitle());

        String[] sc = Util.toDisplay(seek_bar.getSc()).split(":");
        String[] ec = Util.toDisplay(seek_bar.getEc()).split(":");

        edtx_sc_0.setText(sc[0]);
        edtx_sc_1.setText(sc[1]);
        edtx_ec_0.setText(ec[0]);
        edtx_ec_1.setText(ec[1]);

        edtx_sc_0.setEnabled(true);
        edtx_sc_1.setEnabled(true);
        edtx_ec_0.setEnabled(true);
        edtx_ec_1.setEnabled(true);

//        chbx_seek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                try {
//                    if (isChecked) {
//                        edtx_sc_0.setEnabled(false);
//                        edtx_sc_1.setEnabled(false);
//                        edtx_ec_0.setEnabled(false);
//                        edtx_ec_1.setEnabled(false);
//
//
//                        String[] sc = Util.toDisplay(seek_bar.getSc()).split(":");
//                        String[] ec = Util.toDisplay(seek_bar.getEc()).split(":");
//
//                        edtx_sc_0.setText(sc[0]);
//                        edtx_sc_1.setText(sc[1]);
//                        edtx_ec_0.setText(ec[0]);
//                        edtx_ec_1.setText(ec[1]);
//
//                    }
//                    else {
//                        edtx_sc_0.setEnabled(true);
//                        edtx_sc_1.setEnabled(true);
//                        edtx_ec_0.setEnabled(true);
//                        edtx_ec_1.setEnabled(true);
//                    }
//                } catch (Exception e) {
//                    Log.e("", "", e);
//                }
//            }
//        });
//        chbx_seek.setChecked(true);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (edtx_title.getText().toString().trim().isEmpty()) {
                        toast("Please set title");
                        edtx_title.requestFocus();
                        return;
                    }

                    String start = edtx_sc_0.getText().toString() + ":" + edtx_sc_1.getText().toString();
                    String end   = edtx_ec_0.getText().toString() + ":" + edtx_ec_1.getText().toString();


                    if (Util.fromDisplay(start) == -1) {
                        toast("Invalid of start time");
                        edtx_sc_0.requestFocus();
                        return;
                    }

                    if (Util.fromDisplay(end) == -1) {
                        toast("Invalid of end time");
                        edtx_ec_0.requestFocus();
                        return;
                    }

                    if (start.equals(end)) {
                        toast("No selection time");
                        edtx_sc_0.requestFocus();
                        return;
                    }

                    addTrack(edtx_title.getText().toString(), start, end);
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }

        });
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void addTrack(final String p_title, final String p_start, final String p_end) {
        new AsyncTask<String, String, Object[]>() {
            @Override
            protected Object[] doInBackground(String... strings) {
                try {
                    int sq_no = 1;
                    Cursor cursor = DB.query("select sq_no from FILE_TRACK where file_id=" + mFileHolder._id + " order by sq_no desc limit 1");
                    if (cursor.moveToFirst()) {
                        sq_no = cursor.getInt(0) + 1;
                    }

                    int sc, ec;

                    ContentValues cvalues = new ContentValues();
                    cvalues.put("file_id", mFileHolder._id);
                    cvalues.put("title", p_title);
                    cvalues.put("sq_no", sq_no);
                    cvalues.put("sc_time", sc = Util.fromDisplay(p_start));
                    cvalues.put("ec_time", ec = Util.fromDisplay(p_end));
                    long id = DB.insert("FILE_TRACK", cvalues);

                    return new Object[]{Long.valueOf(id).intValue(), p_title, sc, ec};
                } catch (Exception e) {
                    Log.e("", "", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object[] r) {
                try {
                    mAdapterTrack.DATA.add(r);
                    mAdapterTrack.notifyItemInserted(mAdapterTrack.DATA.size() - 1);
                    txt_section_count.setText(mAdapterTrack.DATA.size() + " Sections");
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }
        }.execute();
    }


    private void onActivityResultAddFileFromLibrary(int requestCode, int resultCode, final Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            Log.w(TAG, "onActivityResultAddFileFromLibrary");

            if (mPlayer != null && mPlayer.isPlaying()) {
                btn_play_pause.callOnClick();
            }

            new Thread(){
                @Override
                public void run() {
                    try {
                        int    _id  = data.getIntExtra("_id", 0);
                        String path = data.getStringExtra("path");

                        mFileHolder = FileHolder.find(getActivity(), _id);
                        if (mFileHolder == null) {
                            return;
                        }

                        ContentValues cvalues = new ContentValues();
                        cvalues.put("_id",         _id);
                        cvalues.put("path",        path);
                        cvalues.put("last_update", System.currentTimeMillis());
                        DB.insert("FILE",  cvalues);

                        prepare();

                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            }.start();
        }
    }

    private void selectTrack(Object[] data) {
        if (mSelectedTrack == data) {
            return;
        }

        Object[]
        last_selected  = mSelectedTrack;
        mSelectedTrack = data;

        if (last_selected != null) {
            mAdapterTrack.notifyItemChanged(mAdapterTrack.DATA.indexOf(last_selected));
        }

        mAdapterTrack.notifyItemChanged(mAdapterTrack.DATA.indexOf(mSelectedTrack));

        track_selected.setText((String) data[1]);
        seekTo((int) data[2], (int) data[3]);
    }

    private void nextTrack() {
        if (mAdapterTrack.DATA.isEmpty()) {
            return;
        }

        if (mSelectedTrack == null) {
            selectTrack(mAdapterTrack.DATA.get(0));
            return;
        }

        int idx = mAdapterTrack.DATA.indexOf(mSelectedTrack) + 1;
        if (idx < mAdapterTrack.DATA.size()) {
            selectTrack(mAdapterTrack.DATA.get(idx));
        }
    }

    private void prevTrack() {
        if (mAdapterTrack.DATA.isEmpty()) {
            return;
        }

        if (mSelectedTrack == null) {
            selectTrack(mAdapterTrack.DATA.get(0));
            return;
        }

        int idx = mAdapterTrack.DATA.indexOf(mSelectedTrack) - 1;
        if (idx >= 0) {
            selectTrack(mAdapterTrack.DATA.get(idx));
        }
    }

    private void clearSelection() {
        if (mSelectedTrack == null) {
            return;
        }
        Object[] track = mSelectedTrack;
        mSelectedTrack = null;
        mAdapterTrack.notifyItemChanged(mAdapterTrack.DATA.indexOf(track));
        track_selected.setText("");
    }


    private class Holder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView time;
        public RelativeLayout rvly_layout;
        public ImageButton btn_menu;
        public Indicator play_indicator;

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        public boolean isChecking = false;
        public SparseBooleanArray ITEM_CHECK = new SparseBooleanArray();
        public ArrayList<Object[]> DATA = new ArrayList<>();
        //0:_id 1:title 2:sc 3:ec

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = viewGroup.inflate(viewGroup.getContext(), R.layout.track_adapter, null);
            Holder holder = new Holder(view);
            try {
                holder.title  = view.findViewById(R.id.title);
                holder.time   = view.findViewById(R.id.time);
                holder.rvly_layout = view.findViewById(R.id.rvly_layout);
                holder.btn_menu = view.findViewById(R.id.btn_menu);
                holder.play_indicator = view.findViewById(R.id.play_indicator);
            } catch (Exception e) {
                Log.e("", "", e);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, final int index) {
            try {
                final Object[] data = DATA.get(index);

                holder.title.setText((String) data[1]);
                holder.time .setText(Util.toDisplay((int) data[2]) + " - " + Util.toDisplay((int) data[3]) );

                if (mSelectedTrack == data) {
                    holder.rvly_layout.setActivated(true);
                    if (mPlayer.isPlaying()) {
                        holder.play_indicator.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.play_indicator.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    holder.rvly_layout.setActivated(false);
                    holder.play_indicator.setVisibility(View.INVISIBLE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            selectTrack(data);
                        } catch (Exception e) {
                            Log.e("", "", e);
                        }
                    }
                });

                holder.btn_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openMenu(view, data);
                    }
                });
            } catch (Exception e) {
                Log.e("", "", e);
            }
        }

        @Override
        public int getItemCount() {
            return DATA.size();
        }

        int indexOf(int pId) {
            for (int i = 0; i < DATA.size(); i++) {
                if (pId == (int) DATA.get(i)[0]) {
                    return i;
                }
            }
            return -1;
        }

        public Object[] getItem(int pId) {
            return DATA.get(indexOf(pId));
        }

        public String getNewTitle() {
            int id = 1;
            String title = "Section";
            boolean found;
            do {
                found = false;
                for (Object[] obj : DATA) {
                    if ((title + id).toLowerCase().replaceAll(" ", "").trim().equals(
                            String.valueOf(obj[1]).toLowerCase().replaceAll(" ", "").trim())) {
                        id++;
                        found = true;
                    }
                }
            } while (found);
            return title + " " + id;
        }

        private void openMenu(final View anchor, final Object[] row) {
            final List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object>
//            menu = new HashMap<String, Object>();
//            menu.put("title", "Rename");
//            data.add(menu);
            menu = new HashMap<String, Object>();
            menu.put("title", "Delete");
            data.add(menu);

            menu = new HashMap<String, Object>();
            menu.put("title", "Rename");
            data.add(menu);

            final ListPopupWindow popupWindow = new ListPopupWindow(getContext());

            ListAdapter adapter = new SimpleAdapter(
                    getContext(),
                    data,
                    R.layout.adapter_popup_menu, // You may want to use your own cool layout
                    new String[] {"title"}, // These are just the keys that the data uses
                    new int[] {android.R.id.text1}); // The view ids to map the data to

            popupWindow.setAnchorView(anchor);
            popupWindow.setAdapter(adapter);
            popupWindow.setWidth(dip(120));
            popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if (i == 0) {
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText(row[1] + "")
                                    .setContentText("Won't be able to recover this section!")
                                    .setConfirmText("Yes,delete it!")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            try {
                                                DB.delete("FILE_TRACK", "_id=" + row[0]);
                                                int idx = DATA.indexOf(row);
                                                if (idx > -1) {
                                                    DATA.remove(idx);
                                                    notifyItemRemoved(idx);
                                                    txt_section_count.setText(DATA.size() + " Sections");

                                                    if (mSelectedTrack == row) {
                                                        mSelectedTrack = null;
                                                        track_selected.setText("");
                                                    }
                                                }
                                                sweetAlertDialog.dismissWithAnimation();
                                            } catch (Exception e) {
                                                Log.e(TAG, "", e);
                                            }
                                        }
                                    })
                                    .show();
                        }
                        else if (i == 1) {
                            AdsUtil.showInterstitial(new Runnable() {
                                @Override
                                public void run() {
                                    final EditText edtText = new EditText(getActivity());
                                    edtText.setPadding(dip(8), dip(8), dip(8), dip(8));
                                    edtText.setText(row[1] + "");
                                    edtText.setSelection(edtText.getText().toString().length());
                                    new SweetAlertDialog(getActivity(), SweetAlertDialog.BUTTON_CONFIRM)
                                            .setCustomView(edtText)
                                            .setTitleText("Change section name")
                                            .setConfirmButton("Rename", new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    try {
                                                        row[1] = edtText.getText().toString();
                                                        ContentValues cvalues =  new ContentValues();
                                                        cvalues.put("title", row[1] + "");
                                                        DB.update("FILE_TRACK", cvalues, "_id=" + row[0]);
                                                        int idx = DATA.indexOf(row);
                                                        if (idx > -1) {
                                                            notifyItemChanged(idx);
                                                        }
                                                        sweetAlertDialog.dismissWithAnimation();
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "", e);
                                                    }
                                                }
                                            })
                                            .show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                    popupWindow.dismiss();
                }
            }); // the callback for when a list item is selected
            popupWindow.show();

        }

    }

    private void toast(String message) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }



    int dip(int pValue) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pValue,r.getDisplayMetrics()));
    }


}
