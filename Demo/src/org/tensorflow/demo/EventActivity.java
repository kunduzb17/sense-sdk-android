/*
 * Copyright 2017 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Demonstrates how to run an audio recognition model in Android.

This example loads a simple speech recognition model trained by the tutorial at
https://www.tensorflow.org/tutorials/audio_training

The model files should be downloaded automatically from the TensorFlow website,
but if you have a custom model you can update the LABEL_FILENAME and
MODEL_FILENAME constants to point to your own files.

The example application displays a list view with all of the known audio labels,
and highlights each one when it thinks it has detected one through the
microphone. The averaging of results to give a more reliable signal happens in
the RecognizeCommands helper class.
*/

package org.tensorflow.demo;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import java.util.Arrays;

import ai.cochlear.sdk.core.Cochlear;
import ai.cochlear.sdk.models.Model;
import ai.cochlear.sdk.CochlearException;
import android.support.v4.app.ActivityCompat;


/**
 * An activity that listens for audio and then uses a TensorFlow model to detect particular classes,
 * by default a small set of action words.
 */
public class EventActivity extends Activity {

    // Constants that control the behavior of the recognition code and model
    // settings. See the audio recognition tutorial for a detailed explanation of
    // all these, but you should customize them to match your training settings if
    // you are running your own model.

    private static final int SAMPLE_RATE = 22050;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    private static final int RECORD_BUF_SIZE = SAMPLE_RATE * 4 * 2;
    boolean isRecording = false;
    AudioRecord record;
    Model model;



    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 5;


    // UI elements.
    private static final int REQUEST_RECORD_AUDIO = 13;
    private Button backButton;
    private ListView labelsListView;
    private static final String LOG_TAG = EventActivity.class.getSimpleName();





    private ArrayList<String> displayedLabels = new ArrayList<>();
    private List<String> chosen = new ArrayList<>();
    final static String displayed = "displayed_labels";
    private boolean[] checked_list;
    private int[] labelIndexMapper;
    String tag = null;

    private final String apiKey = "zSBN19oxe1dLN3d25hWuTLNC3b5jEaWQuDVbGuFL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the UI.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        Cochlear cochlear = Cochlear.getInstance();
        cochlear.init(getApplicationContext(), apiKey);

        labelsListView = (ListView) findViewById(R.id.list_view2);

        Intent intent=new Intent(this.getIntent());
        checked_list = intent.getBooleanArrayExtra("checked_list");
        displayedLabels = intent.getStringArrayListExtra(displayed);
        System.out.println(displayedLabels.get(0) + " "+ displayedLabels.get(1));

        backButton = (Button)findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                EventActivity.this.finish();
            }
        });


        // Load the labels for the model, but only display those that don't start
        // with an underscore.


        labelIndexMapper = new int[checked_list.length];
        for(int i=0; i<checked_list.length; i++){
            if(true==checked_list[i]){
                labelIndexMapper[i] = chosen.size();
                chosen.add(displayedLabels.get(i));
            }else{
                labelIndexMapper[i] = -1;
            }
        }


        // Build a list view based on these labels.
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, R.layout.list_text_item, chosen);
        labelsListView.setAdapter(arrayAdapter);


        // Load the TensorFlow model.
        model = Cochlear.getInstance().getModel("event");

        // Start the recording and recognition threads.
        if (!requestPermission()) {
            return;
        }
        startRecording();
    }




    public void startRecording() {
        isRecording = true;
        record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                RECORD_BUF_SIZE);
        model.addInput(record);

        // Predict audio stream
        model.predict(new Model.OnPredictListener() {
            @Override
            public void onPredictDone(JSONObject result) {
                JSONObject result1 = null;
                try {
                    result1 = result.getJSONObject("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray frames = null;
                try {
                    frames = result1.getJSONArray("frames");
                    //Log.d(LOG_TAG, frames.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject arr = null;
                try {
                    arr = frames.getJSONObject(0);
                    //Log.d(LOG_TAG, frames.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    tag = arr.getString("tag");
                    Log.d(LOG_TAG, tag);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                for (int i = 0; i < chosen.size(); i++) {
                                    if(chosen.get(i).equals(tag)){
                                        int idx = labelIndexMapper[i];
                                        View labelView = labelsListView.getChildAt(i);

                                        AnimatorSet colorAnimation =
                                                (AnimatorSet)
                                                        AnimatorInflater.loadAnimator(
                                                                EventActivity.this, R.animator.color_animation);
                                        colorAnimation.setTarget(labelView);
                                        colorAnimation.start();

                                    }

                                }
                            }
                        });

//                try {
//                    // We don't need to run too frequently, so snooze for a bit.
//                    Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
//                } catch (InterruptedException e) {
//                    // Ignore
//                }


                Log.d(LOG_TAG, result.toString());
            }

            @Override
            public void onError(CochlearException error) {
                Log.d(LOG_TAG, error.toString());
            }
        });
        for(int i= 0; i<chosen.size(); i++ ){
           System.out.println(chosen.get(i));
        }




    }
    public void stopRecording() {
        isRecording = false;
        model.stopPredict();

    }
    private boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
            };

            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



}
