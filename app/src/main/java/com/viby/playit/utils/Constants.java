package com.viby.playit.utils;

public class Constants {
    public interface Action {
        String MAIN_ACTION = "com.viby.playit.service.MediaplayerService.action.main";
        String INIT_ACTION = "com.viby.playit.service.MediaplayerService.action.init";
        String PREV_ACTION = "com.viby.playit.service.MediaplayerService.action.prev";
        String PLAY_ACTION = "com.viby.playit.service.MediaplayerService.action.play";
        String PAUSE_ACTION = "com.viby.playit.service.MediaplayerService.action.pause";
        String NEXT_ACTION = "com.viby.playit.service.MediaplayerService.action.next";
        String STARTFOREGROUND_ACTION = "com.viby.playit.service.MediaplayerService.action.START_FOREGROUND";
        String STOPFOREGROUND_ACTION = "com.viby.playit.service.MediaplayerService.action.STOP_FOREGROUND";
    }

    public interface Notify {
        String MEDIAPLAYER_PLAY = "com.viby.playit.activities.MainActivity.notify.mediaplayerplay";
        String NEXT_SONG = "com.viby.playit.activities.MainActivity.notify.nextsong";
        String END_STATE = "com.viby.playit.activities.MainActivity.notify.end";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface Repeat {
        int REPEAT_DISABLE = 0;
        int REPEAT_ALL = 1;
        int REPEAT_SAME = 2;
    }

    public interface Permissions {
        int WRITE_EXTERNAL_STORAGE = 101;
    }
}
