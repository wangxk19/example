package com.shd.boomtruckpad.util;

import android.net.Uri;
import android.widget.VideoView;

public class RtspUtils {
    private void PlayRtspStream(VideoView vv,String rtspUrl){
        vv.setVideoURI(Uri.parse(rtspUrl));
        vv.requestFocus();
        vv.start();
    }
}
