package com.viby.playit.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.viby.playit.models.Song;

import java.util.ArrayList;

public class MusicUtil {
    public static void createSongPlaylist(Context context, ArrayList<Song> list) {
        ArrayList<String> tmpFilesPath = new ArrayList<>();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ALBUM
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE"
        );

        while (cursor.moveToNext()) {
            if (!tmpFilesPath.contains(cursor.getString(0).substring(cursor.getString(0).lastIndexOf("/") + 1)
                    + ":"
                    + cursor.getString(2))) {

                tmpFilesPath.add(cursor.getString(0).substring(cursor.getString(0).lastIndexOf("/") + 1)
                        + ":"
                        + cursor.getString(2)
                );

                Song song = new Song(cursor.getString(0));
                Long duration = Long.parseLong(cursor.getString(2));

                song.setTitle(cursor.getString(1));
                song.setLength(duration / 1000);
                song.setArtist(cursor.getString(3));
                song.setYear(cursor.getString(4));
                song.setAlbum(cursor.getString(5));

                list.add(song);
            }
        }

        cursor.close();
    }
}
