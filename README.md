<img src="http://cochlear.ai/wp-content/uploads/2017/09/short_black.png" width="512">

# Cochlear.ai Sense SDK for Android

Hello and welcome! This is the public repository of the Cochlear.ai SDK for Android devices.

With this SDK we can help you to bring the power of A.I. to mobile applications. Check our developer's site at [https://cochlear.ai/docs](https://cochlear.ai/docs) or contact us at <support@cochlear.ai> to learn more.

# Getting started

In order to run the SDK, you would require sense API key. Please [subscribe](https://cochlear.ai/beta-subscription/) sense API before proceeding with this guide.

Sense Android SDK supports applications running on Android API 26 (Version 8.0 “Oreo”), or later.

### Install the SDK with an *.aar

First, place the `sense-sdk.aar` library file into the modules's `libs` folder of your application, creating one if necessary.

In the **app-level** `build.gradle` file, add the following:

```gradle
android {
    ...
    aaptOptions {
        noCompress "model"
    }
}

repositories {
    flatDir {
        dirs 'libs'
    }
}
...
dependencies {
    ...
    implementation (name:'sense-sdk', ext:'aar')

    implementation 'org.tensorflow:tensorflow-lite:0.0.0-nightly'
    implementation 'org.tensorflow:tensorflow-lite-gpu:0.0.0-nightly'
    ...
}
```

In the `AndroidManifest.xml` file, add the following:

```
<uses-permission android:name="android.permission.INTERNET"/>
```

  * For the audio **stream**
  ```
  <uses-permission android:name="android.permission.RECORD_AUDIO"/>
  ```

  * For the audio **file**
  ```
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  ```

### Start the sense SDK

The sense SDK is initialized by calling `Cochlear.init(context, apiKey);` within the `ai.cochlear.sdk.core.Cochlear` package. We recommend starting it when your app has finished launching, but that is not absolutely required. Furthermore, work is offloaded to background threads, so there should be little to no impact on the launching of your app.

```java
import ai.cochlear.sdk.core.Cochlear;

public class MainActivity extends AppCompatActivity {
  private final String apiKey = "ENTER YOUR API KEY";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Cochlear cochlear = Cochlear.getInstance();
    cochlear.init(getApplicationContext(), apiKey);
  }
}
```

### Load Models On Device

Sense SDK has a variety of pre-built models to predict against. However, only the `Event` Model is readily available within the SDK. For access to other models please contact support@cochlear.ai. The sample code below shows how to load the `Event` Model and make a prediction against it.

```java
import ai.cochlear.sdk.models.Model;
...

// Get Event Model
Model model = Cochlear.getInstance().getModel("event");
```

### Add Device Inputs with SDK

The SDK is built around a simple idea. You give inputs (audio) to the library and it returns a result of predictions (JSON). You need to add inputs to make predictions on it.

There are two input types, `file` and `stream`. For the audio file prediction, you have to put the `File` object as a parameter of `addInput()` method. For the audio stream prediction, you have to put the `AudioRecord` object as a paramter of `addInput()` method.

* Stream

```java
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
...

// Set the parameters for the AudioRecord object
private static final int SAMPLE_RATE = 22050;
private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
private static final int RECORD_BUF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;

// Create a AudioRecord object and added it
model.addInput(new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                               SAMPLE_RATE,
                               CHANNEL_CONFIG,
                               AUDIO_FORMAT,
                               RECORD_BUF_SIZE));

...
```

* File

```java
import java.io.File;
...

// Storage which is in the audio file
File sdcard = Environment.getExternalStorageDirectory();

// Create a File object and added it
model.addInput(new File(sdcard,"some_audio_file.mp3"));
```

### Predict On Device

Just as with our API, you can use the predict functionality on device with any of our available pre-built models. Predictions generate JSON format outputs. An output format has a similar structure to an sense API result. The JSON result contains the predictions and their probability.

* JSON result format

```
{
    "result": {
        "frames"   : [
            {
                "tag"           : "<CLASS NAME>",
                "probability"   : <Probability value (float) for 'CLASS NAME'>,
                "start_time"    : <Prediction start time in audio file>,
                "end_time"      : <Prediction end time in audio file>,
            },
        ],
        "task"      : "<TASK NAME>",
    },

    "status"        : {
        "code"          : <Status code>,
        "description"   : "<Status code description>"
    }
}
```

Note that the prediction results from pre-built models on the SDK may differ from those on the API. Specifically, there may be a loss of accuracy up to 3~5% due to the conversion of the models that allow them to be compact enough to be used on lightweight devices. This loss is expected within the current industry standards.

```java
import ai.cochlear.sdk.core.Cochlear;
import ai.cochlear.sdk.models.Model;
import ai.cochlear.sdk.CochlearException;
...

// Set the parameters for the AudioRecord object
private static final int SAMPLE_RATE = 22050;
private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
private static final int RECORD_BUF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;

AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                                     SAMPLE_RATE,
                                     CHANNEL_CONFIG,
                                     AUDIO_FORMAT,
                                     RECORD_BUF_SIZE));

// Use the model you want to predict on. The model in the sample code
// below is our Event Model.
final Model model = Cochlear.getInstance().getModel("event");
model.addInput(record);

// Predict audio stream
model.predict(new Model.OnPredictListener() {
    @Override
    public void onPredictDone(JSONObject result) {
        Log.d(LOG_TAG, result.toString());
    }

    @Override
    public void onError(CochlearException error) {
        Log.d(LOG_TAG, error.toString());
    }
});

```

## Learn and do more

Check out our [documentation site](https://cochlear.ai/docs) to learn a lot more about how to bring the audio A.I. to your app.


## Support

Questions? Have an issue? Send us a message at <support@cochlear.ai>.


## License

TODO
