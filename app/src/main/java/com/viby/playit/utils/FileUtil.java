package com.viby.playit.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static boolean createMusicListFiles(List<File> fileArrayList, String path) {
        if (fileArrayList == null)
            return false;

        File storageDir = new File(path);
        File musicDir  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        if (storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    fileArrayList.add(file);
                } else {
                    fileArrayList.addAll(getFiles(file));
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static List<File> getFiles(File dir) {
        List<File> files = new ArrayList<>();
        File[] buffer;

        if (dir.isDirectory()) {
            buffer = dir.listFiles();

            for (File file : buffer) {
                if (file.isDirectory()) {
                    files.addAll(getFiles(file));
                } else {
                    if (isMusicFile(file))
                        files.add(file);
                }
            }

            return files;
        } else {
            return files;
        }
    }

    public static boolean isMusicFile(File file) {
        String fileName = file.getName();
        String[] fileNameSplit = fileName.split("\\.");

        return "mp3".equals(fileNameSplit[fileNameSplit.length - 1]);
    }
}
