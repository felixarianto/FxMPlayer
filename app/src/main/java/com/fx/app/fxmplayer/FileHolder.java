package com.fx.app.fxmplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileHolder {

    public String name;
    public String path;
    public String album_id;
    public String album_art;
    public String artist;
    public int _id;
    public int    duration;
    public boolean isSeparator = false;


    public static FileHolder find(Context context, int _id) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri    = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(
                 uri,
                null,
                MediaStore.Audio.Media._ID + "=" + _id,
                null,
                MediaStore.Audio.Media.TITLE + " ASC LIMIT " + 1,
                null
        );

        if (cursor.moveToNext()) {
            FileHolder holder = new FileHolder();
            holder._id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            holder.name     = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            holder.artist   = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            holder.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            holder.path     = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            holder.album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            Cursor cursor_art = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + "=?",
                    new String[] {holder.album_id},
                    null);

            if (cursor_art.moveToFirst()) {
                holder.album_art = cursor_art.getString(cursor_art.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }

            return holder;
        }
        return null;
    }

}
