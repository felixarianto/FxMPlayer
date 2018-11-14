package com.fx.app.fxmplayer;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fx.app.sqlite.DB;

import java.io.File;
import java.util.ArrayList;

public class FragmentMain extends FragmentView implements View.OnClickListener , MediaPlayer.OnPreparedListener {


    public FragmentMain() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.fragment_main);
        /*
         * INFO
         */
        txt_title = findViewById(R.id.txt_title);
        txt_subtitle = findViewById(R.id.txt_subtitle);

        /*
         * LIST
         *
         */
        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);

        rcvw_track = findViewById(R.id.rcvw_track);
        rcvw_track.setAdapter(mAdapterTrack = new Adapter());

        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.divider_list));
        rcvw_track.addItemDecoration(divider);
        rcvw_track_layout_manager = (LinearLayoutManager) rcvw_track.getLayoutManager();

        /*
         * SEEKER
         *
         */

        btn_play_pause = findViewById(R.id.btn_play_pause);
        btn_play_pause.setOnClickListener(this);

        seek_total = findViewById(R.id.seek_total);
        seek_line = findViewById(R.id.seek_line);
        seek_start = findViewById(R.id.seek_start);
        seek_end   = findViewById(R.id.seek_end);
        track_selected = findViewById(R.id.track_selected);

        seek_pos_sc = findViewById(R.id.seek_pos_sc);
        seek_pos_ec = findViewById(R.id.seek_pos_ec);

        seek_pos_sc.setOnTouchListener(new SeekTouchListener(){
            @Override
            public int min() {
                return 0;
            }

            @Override
            public int max() {
                return max_seek_margin - 20;
            }

            @Override
            public void changed(int margin_position) {
                min_seek_margin = margin_position;

                int sc = min_seek_margin == 0 ? 0 : mPlayer.getDuration() * min_seek_margin / seek_total.getWidth();
                seek_start.setText(Util.toDisplay(min_seek = sc));
                mPlayer.seekTo(min_seek);
            }
        });
        seek_pos_ec.setOnTouchListener(new SeekTouchListener(){
            @Override
            public int min() {
                return min_seek_margin + 20;
            }

            @Override
            public int max() {
                return seek_total.getWidth();
            }

            @Override
            public void changed(int margin_position) {
                max_seek_margin = margin_position;

                int ec = max_seek_margin == 0 ? 0 : mPlayer.getDuration() * max_seek_margin / seek_total.getWidth();
                seek_end  .setText(Util.toDisplay(max_seek = ec));

            }
        });

        /*
         *
         */
        openLast();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    Cursor cr = DB.query("select path, _id from FILE order by last_update desc limit 1");
                    if (cr.moveToFirst()) {
                        prepare(new File(cr.getString(0)));
                        mFileId = cr.getInt(1);
                    }
                    cr.close();

                    mAdapterTrack.DATA.clear();
                    cr = DB.query("select _id, title, sc_time, ec_time from FILE_TRACK order by sq_no desc");
                    while (cr.moveToNext()) {
                        mAdapterTrack.DATA.add(new Object[]{cr.getInt(0), cr.getString(1), cr.getInt(2), cr.getInt(3)});
                    }
                    if (rcvw_track.getHandler() != null) {
                        rcvw_track.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapterTrack.notifyDataSetChanged();
                            }
                        });
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
    private ImageButton btn_add;
    private TextView txt_title;
    private TextView txt_subtitle;

    private ImageButton btn_play_pause;
    private RecyclerView rcvw_track;
    private LinearLayoutManager rcvw_track_layout_manager;
    private int mSelectedTrack = -1;

    private void prepare(File file) throws Exception {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        mInfo= new MediaMetadataRetriever();
        mInfo.setDataSource(file.getAbsolutePath());

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(FragmentMain.this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDataSource(getActivity(), Uri.fromFile(file));
        mPlayer.prepare();
    }


    private View seek_pos_sc;
    private View seek_pos_ec;
    private TextView seek_start;
    private TextView seek_end;
    private View seek_total;
    private View seek_line;
    private Runnable mSeekRunnable;
    private TextView track_selected;

    private int min_seek = 0;
    private int max_seek = 0;
    private int min_seek_margin = 0;
    private int max_seek_margin = 0;
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
                    //8dp is size of circle seek
                    ViewGroup.LayoutParams params = seek_line.getLayoutParams();
                    params.width = (max_seek_margin - dip(8) - min_seek_margin) * (mPlayer.getCurrentPosition() - min_seek) / (max_seek - min_seek);
                    seek_line.setLayoutParams(params);

                    seek_start.setText(Util.toDisplay(mPlayer.getCurrentPosition()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (max_seek <= mPlayer.getCurrentPosition()) {
                    mPlayer.seekTo(min_seek);//REPEAT
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

    private void seekSelect(int sc, int ec) {
        min_seek = sc;
        max_seek = ec;

        seek_start.setText(Util.toDisplay(sc));
        seek_end  .setText(Util.toDisplay(ec));

        ViewGroup.LayoutParams params = seek_line.getLayoutParams();
        params.width = 0;
        seek_line.setLayoutParams(params);

        int p_sc, p_ec;

        RelativeLayout.LayoutParams
                sc_params = (RelativeLayout.LayoutParams) seek_pos_sc.getLayoutParams();
        sc_params.setMargins(p_sc = seek_total.getWidth() * sc / mPlayer.getDuration(), 0, 0, 0);
        seek_pos_sc.setLayoutParams(sc_params);

        RelativeLayout.LayoutParams
                ec_params = (RelativeLayout.LayoutParams) seek_pos_ec.getLayoutParams();
        ec_params.setMargins(p_ec = seek_total.getWidth() * ec / mPlayer.getDuration(), 0, 0, 0);
        seek_pos_ec.setLayoutParams(ec_params);

        min_seek_margin = p_sc;
        max_seek_margin = p_ec;

        mPlayer.seekTo(sc);
    }


    private void delTrack() {
//        mAdapterTrack.isChecking = true;
//        notifyVisible();
//        lnly_menu_confirm.setVisibility(View.VISIBLE);
//        btn_menu_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    int size = mAdapterTrack.ITEM_CHECK.size();
//                    for (int i = 0; i < size; i++) {
//                        int id = mAdapterTrack.ITEM_CHECK.keyAt(i);
//                        if (id >= 0 && mAdapterTrack.ITEM_CHECK.get(id, false)) {
//                            Object[] data = mAdapterTrack.getItem(id);
//                            DB.delete("FILE_TRACK", "_id=" + data[0]);
//                            int idx = mAdapterTrack.indexOf(id);
//                            mAdapterTrack.DATA.remove(idx);
//                            mAdapterTrack.notifyItemRemoved(idx);
//                        }
//                    }
//
//                    closeMenuTrack();
//                } catch (Exception e) {
//                    Log.e("", "", e);
//                }
//            }
//        });
    }

    private void notifyVisible() {
        if (!mAdapterTrack.DATA.isEmpty()) {
            mAdapterTrack.notifyItemRangeChanged(0, mAdapterTrack.DATA.size());
        }
    }

    private void addTrack() {
        final Dialog dialog = new Dialog(getActivity());
        View view = View.inflate(getActivity(), R.layout.track_add_dialog, null);
        final EditText edtx_title = view.findViewById(R.id.edtx_title);
        final EditText edtx_start = view.findViewById(R.id.edtx_start);
        final EditText edtx_end   = view.findViewById(R.id.edtx_end);
        Button   btn_save   = view.findViewById(R.id.btn_save);
        Button   btn_cancel = view.findViewById(R.id.btn_cancel);
        CheckBox chbx_seek  = view.findViewById(R.id.chbx_selection);

        edtx_title.setText(mAdapterTrack.getNewTitle());

        chbx_seek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        edtx_start.setEnabled(false);
                        edtx_end  .setEnabled(false);
                        edtx_start.setText(Util.toDisplay(min_seek));
                        edtx_end  .setText(Util.toDisplay(max_seek));
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

                    if (Util.fromDisplay(edtx_start.getText().toString()) == -1) {
                        toast("Invalid of start time");
                        edtx_start.requestFocus();
                        return;
                    }

                    if (Util.fromDisplay(edtx_end.getText().toString()) == -1) {
                        toast("Invalid of end time");
                        edtx_end.requestFocus();
                        return;
                    }

                    if (Util.fromDisplay(edtx_start.getText().toString()) == Util.fromDisplay(edtx_end.getText().toString())) {
                        toast("No selection time");
                        edtx_end.requestFocus();
                        return;
                    }

                    addTrack(edtx_title.getText().toString(), edtx_start.getText().toString(), edtx_end.getText().toString());
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
                    Cursor cursor = DB.query("select sq_no from FILE_TRACK where file_id=" + mFileId + " order by sq_no desc limit 1");
                    if (cursor.moveToFirst()) {
                        sq_no = cursor.getInt(0) + 1;
                    }

                    int sc, ec;

                    ContentValues cvalues = new ContentValues();
                    cvalues.put("file_id", mFileId);
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
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }
        }.execute();
    }

    private void addFile() throws Exception {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,REQUEST_CODE_ADD_FILE);
    }
    private void onActivityResultAddFile(int requestCode, int resultCode, final Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        String path = FileUtil.getPath(getActivity(), data.getData());

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
        seekSelect(0, mPlayer.getDuration());
        txt_title.setText(mInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + " " + mInfo.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
    }

    private class Holder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView time;
        public CheckBox checkbox;

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
                holder.checkbox = view.findViewById(R.id.chbx_selection);
            } catch (Exception e) {
                Log.e("", "", e);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, final int index) {
            try {
                final Object[] data = DATA.get(index);

                final int _id = (int) data[0];
                holder.title.setText((String) data[1]);
                holder.time .setText(Util.toDisplay((int) data[2]) + " - " + Util.toDisplay((int) data[3]) );

                if (isChecking) {
                    holder.title.setEnabled(false);
                    holder.time .setEnabled(false);
                    holder.checkbox.setVisibility(View.VISIBLE);
                    holder.checkbox.setChecked(ITEM_CHECK.get(_id, false));
                    holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            ITEM_CHECK.put(_id, isChecked);
                        }
                    });

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                ITEM_CHECK.put(_id, !ITEM_CHECK.get(_id, false));
                                holder.checkbox.setChecked(ITEM_CHECK.get(_id, false));
                            } catch (Exception e) {
                                Log.e("", "", e);
                            }
                        }
                    });
                }
                else {
                    boolean is_selected =  _id == mSelectedTrack;

                    holder.title.setEnabled(is_selected);
                    holder.time .setEnabled(is_selected);

                    holder.checkbox.setVisibility(View.GONE);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (mSelectedTrack == (int) data[0]) {
                                    return;
                                }

                                if (mPlayer.isPlaying()) {
                                    btn_play_pause.callOnClick();
                                }

                                int last_selected = mSelectedTrack;
                                mSelectedTrack    = (int) data[0];

                                mAdapterTrack.notifyItemChanged(indexOf(mSelectedTrack));
                                if (last_selected != -1) mAdapterTrack.notifyItemChanged(indexOf(last_selected));

                                track_selected.setText((String) data[1]);
                                seekSelect((int) data[2], (int) data[3]);
                            } catch (Exception e) {
                                Log.e("", "", e);
                            }
                        }
                    });
                }
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
            String title = "Track";
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

    }

    private void toast(String message) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private class SeekTouchListener implements View.OnTouchListener {

        int prevX, prevY;

        public boolean onTouch(View view, MotionEvent event) {
            final RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) view.getLayoutParams();
            switch(event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                {
                    int margin = par.leftMargin + (int) event.getRawX()-prevX;
                    if (margin < min()) {
                        margin = min();
                    }
                    else if (margin > max()) {
                        margin = max();
                    }
                    par.leftMargin = margin;
                    prevX = (int) event.getRawX();
                    view.setLayoutParams(par);
                    changed(par.leftMargin);
                    return true;
                }
                case MotionEvent.ACTION_UP:
                {
                    view.setPressed(false);
                    return true;
                }
                case MotionEvent.ACTION_DOWN:
                {
                    view.setPressed(true);
                    prevX=(int)event.getRawX();
                    prevY=(int)event.getRawY();

                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
                    }else{
                        //deprecated in API 26
                        v.vibrate(100);
                    }
                    return true;
                }
            }
            return false;
        }

        public int min() {
            return 0;
        }

        public int max() {
            return 0;
        }

        public void changed(int margin_position) {

        }
    }

    int dip(int pValue) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pValue,r.getDisplayMetrics()));
    }


}
