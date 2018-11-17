package com.pizz.wifibotcontroller;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;

/**
 * Created by bemunoz2 on 27/01/2017.
 */

public class VideoFragment extends Fragment {

    private MjpegView mjpegView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mjpegView = new MjpegView(getActivity());
        new DoRead().execute(ControllerSingleton.getCommandSender().getVideoUrl());
        return mjpegView;
    }

    @Override
    public void onStop() {
        mjpegView.stopPlayback();
        super.onStop();
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (res.getStatusLine().getStatusCode() == 401) {
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mjpegView.setSource(result);
            mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mjpegView.showFps(false);
        }
    }
}
