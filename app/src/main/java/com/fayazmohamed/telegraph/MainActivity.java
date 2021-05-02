package com.fayazmohamed.telegraph;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.fayazmohamed.telegraph.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    String convertType = "";
    Boolean isAudiOut = true,isFlashOut = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count > 0 && (count == 1 || convertType.equals(""))){
                    updateInputType(s.charAt(count -1));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null){
                    if (convertType.equals("morse")){
                        convertToMorse(s.toString());
                    } else if (convertType.equals("text")){
                        convertToText(s.toString());
                    } else {
                        updateOutputUi("");
                    }
                } else {
                    updateOutputUi("");
                }
            }
        });

        binding.audioToggle.setOnClickListener(v -> {
            if (isAudiOut){
                binding.soundIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.grey1), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                binding.soundIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            isAudiOut = !isAudiOut;
        });
        binding.flashToggle.setOnClickListener(v -> {
            if (isFlashOut){
                binding.torchIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.grey1), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                binding.torchIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
            isFlashOut = !isFlashOut;
        });

        binding.playBtnUpper.setOnClickListener(v -> {
            if (convertType.equals("morse"))
                playOutput(binding.outputField.getText() == null ? "" : binding.outputField.getText().toString());
        });
    }

    void updateInputType(char c){

        boolean isAlphaNum = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '1');
        boolean isMorse = c == '.' || c == '-' || c == '/';

        if (isAlphaNum)
            convertType = "morse";
        else if (isMorse)
            convertType = "text";
        else
            convertType = "";

    }

    void convertToText(String s){
        String[] inp = s.split(" ");
        ArrayList<String> morseList = new ArrayList<>(Arrays.asList(inp));
        String output = MorseCodeHelper.convertToLetter(morseList);
        updateOutputUi(output);
    }

    void convertToMorse(String s){
        if (s != null){
            ArrayList<String> morseOut = MorseCodeHelper.convertToMorse(s);
            StringBuilder sb = new StringBuilder();
            for(String i : morseOut){
                sb.append(i).append(" ");
            }
            updateOutputUi(sb.toString());
        }
    }

    void updateOutputUi(String s){
        binding.outputField.setText(s == null ? "" : s);
    }


    Boolean isPlaying = false;
    void playOutput(String s){

        initAudio();
        initVibration();
        initFlash();

        if (s != null && !s.equals("")) {
            isPlaying = true;
            char[] charArray = s.toCharArray();
            Thread thread = new Thread((new Runnable() {
                @Override
                public void run() {
                    playBtnPushAnim();
                    flash(false);
                    SystemClock.sleep(200);
                    for (char c : charArray) {
                        updateLiveTxtOutput(c);
                        if (c == '.') {
                            if (isFlashOut)
                                flash(true);
                            if (isAudiOut){
                                tone(100);
                                vibrate(100);
                            }
                            SystemClock.sleep(100);
                        } else  if (c == '-'){
                            if (isFlashOut)
                                flash(true);
                            if (isAudiOut){
                                tone(300);
                                vibrate(300);
                            }
                            SystemClock.sleep(300);
                        } else if (c == '/'){
                            if (isFlashOut)
                                flash(false);
                            SystemClock.sleep(100);
                        }
                        updateLiveTxtOutput(' ');
                        flash(false);
                        SystemClock.sleep(100);
                    }
                    flash(false);
                    playBtnPullAnim();
                }
            }));

            thread.start();
        }
    }


    CameraManager cameraManager;
    String mCameraId;
    void initFlash(){
        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            Log.d("DEBUG", "No Flash Available");
            return;
        }

        //getting the camera manager and camera id
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    ToneGenerator toneGenerator;
    void initAudio(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = (int) (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 ) / 15;
        toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, volume);
    }

    Vibrator vibrator;
    void initVibration(){
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }


    void flash(Boolean value){
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            try {
                                cameraManager.setTorchMode(mCameraId, value);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }

    void tone(int duration){
        toneGenerator.startTone(ToneGenerator.TONE_SUP_RADIO_ACK,duration);
    }

    void vibrate(int duration){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }

    void updateLiveTxtOutput(char c){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                binding.outputLiveField.setText(c + "");
            }
        });
    }

    int pushBtnDuration = 200,pullBtnDuration = 200;
    void playBtnPushAnim(){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                MarginProxy marginProxy = new MarginProxy(binding.playBtnUpper);

                int marginRight = marginProxy.getRightMargin();
                int marginbottom = marginProxy.getBottomMargin();

                ObjectAnimator animatorX = ObjectAnimator.ofFloat(binding.playBtnUpper, "translationX", marginRight);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(binding.playBtnUpper, "translationY", marginbottom);
                animatorX.setDuration(pushBtnDuration);
                animatorY.setDuration(pushBtnDuration);
                animatorX.start();
                animatorY.start();

                binding.playBtnUpper.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.purpleDark));
            }
        });

    }

    void playBtnPullAnim(){

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animatorX = ObjectAnimator.ofFloat(binding.playBtnUpper, "translationX", 0);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(binding.playBtnUpper, "translationY", 0);
                animatorX.setDuration(pullBtnDuration);
                animatorY.setDuration(pullBtnDuration);
                animatorX.start();
                animatorY.start();
                binding.playBtnUpper.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.purple));
            }
        },200);


    }
}