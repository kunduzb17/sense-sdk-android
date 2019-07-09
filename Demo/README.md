# Event Android Demo
By Hyungui Lim. If you have any question, ask me.


### 0. Set your andoird studio configures(sdk,apk) by yourself.


### 1. Put model and labels in 'assets/' folder

- test_multi1.pb & test_multi2.pb
	
	two tensorflow models(.pb) for mean ensemble. 

- saved_labels_multi_softmax.txt (Actually it is unnecessary)
	
	original model output labels.

- new_saved_labels_multi_softmax.txt

	modified(merged or filtered) model output labels. 


### 2. Change 'src/org/tensorflow/demo/Event_Activity.java'

- Initial variables

	SAMPLE_DURATION_MS
	
	DETECTION_THRESHOLD
	
	MODEL_FILENAME

	MODEL_FILENAME2
	
	INPUT_DATA_NAME : need to check model input name
	
	OUTPUT_SCORES_NAME : need to check model output name
	
	OUTPUT_LAYER_SIZE : original model output layer size before postprocessing

- For post processing

	line 310 : change customThreshold
	
	line 354~388 : event post processing (merging or filtering)
