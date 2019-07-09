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
the helper class.
*/

package org.tensorflow.demo;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ai.cochlear.sdk.core.Cochlear;

/**
 * An activity that listens for audio and then uses a TensorFlow model to detect particular classes,
 * by default a small set of action words.
 */
public class MainActivity extends Activity {

    // Constants that control the behavior of the recognition code and model
    // settings. See the audio recognition tutorial for a detailed explanation of
    // all these, but you should customize them to match your training settings if
    // you are running your own model.


    private static final String LABEL_FILENAME = "file:///android_asset/new_saved_labels_multi_softmax.txt"; //////

    // UI elements.

    //private ListView labelsListView;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Working variables.

    private List<String> labels = new ArrayList<String>();
    private ArrayList<String> displayedLabels = new ArrayList<>(40);
    final static String displayed = "displayed_labels";


    private boolean[] checked_list;

    private final String apiKey = "zSBN19oxe1dLN3d25hWuTLNC3b5jEaWQuDVbGuFL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the UI.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Cochlear cochlear = Cochlear.getInstance();
        cochlear.init(getApplicationContext(), apiKey);

        //labelsListView = (ListView) findViewById(R.id.list_view);
        final LinearLayout scrollView = (LinearLayout) findViewById(R.id.scrollView);




        // Load the labels for the model, but only display those that don't start
        // with an underscore.
        String actualFilename = LABEL_FILENAME.split("file:///android_asset/")[1];
        Log.i(LOG_TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        // Build a list view based on these labels.

        checked_list = new boolean[displayedLabels.size()];

        for(int i = 0; i<displayedLabels.size(); i++){

            String label = displayedLabels.get(i);


            TextView tv = (TextView) getLayoutInflater().inflate(R.layout.custom_textview, null);

            tv.setText(label);


            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = scrollView.indexOfChild(view);

                    checked_list[index] = !checked_list[index];
                    if(true==checked_list[index])
                        view.setBackgroundColor(Color.CYAN);
                    else
                        view.setBackgroundColor(Color.WHITE);

                }
            });

            scrollView.addView(tv, i);

        }




        Button btDone = (Button)findViewById(R.id.done);
        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                Intent intent=new Intent(MainActivity.this, EventActivity.class);
                intent.putExtra("checked_list", checked_list);
                intent.putStringArrayListExtra(displayed, displayedLabels);
                startActivity(intent);

            }
        });

    }


}
