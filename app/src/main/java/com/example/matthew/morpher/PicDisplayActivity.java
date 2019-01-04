package com.example.matthew.morpher;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Activity that displays the resulting morph
 */
public class PicDisplayActivity extends AppCompatActivity {
    //Activity members
    ImageButton backButton;
    ImageButton prevImgBtn;
    ImageButton playImgBtn;
    ImageButton nextImgBtn;
    ImageView imgViewMorph;
    Bitmap[] bmpArr;
    int interFrameAmount;
    int currentIndex = 0;
    boolean endOfPics = false;

    //Used for the play timing
    Handler mHandler;
    public void useHandler(){
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1000);
    }

    private Runnable mRunnable = new Runnable(){

        @Override
        public void run(){
            if(endOfPics)
                return;
            nextImage();
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    @Override
    /**
     * Occurs when the activity is created
     * @param savedInstanceState Information passed to this activity
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.view_screen);

        backButton = (ImageButton) findViewById(R.id.BackButton);
        backButton.setOnClickListener(backHandle);

        prevImgBtn = (ImageButton) findViewById(R.id.prevImageButton);
        prevImgBtn.setOnClickListener(prevImgHandle);

        playImgBtn = (ImageButton) findViewById(R.id.playImageButton);
        playImgBtn.setOnClickListener(playImgHandle);

        nextImgBtn = (ImageButton) findViewById(R.id.nextImageButton);
        nextImgBtn.setOnClickListener(nextImgHandle);

        Bundle extras = getIntent().getExtras();

        interFrameAmount = extras.getInt("frameNum");
        System.out.println("Intermediate frames: " + interFrameAmount);

        imgViewMorph = (ImageView) findViewById(R.id.imageViewMorph);

        bmpArr = new Bitmap[interFrameAmount + 2];

        for(int i = 0; i < interFrameAmount + 2; i++) {
            FileInputStream fis;
            try {
                fis = openFileInput("image" + i);
                bmpArr[i] = BitmapFactory.decodeStream(fis);
                fis.close();
                System.out.println("opened" + i);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        imgViewMorph.setImageBitmap(bmpArr[currentIndex]);

    }

    /**
     * Event handler for the back button
     */
    View.OnClickListener backHandle = new View.OnClickListener(){
        public void onClick(View v){
            finish();
        }
    };

    /**
     * Event handler for the previous image button
     */
    View.OnClickListener prevImgHandle = new View.OnClickListener(){
        public void onClick(View v){
            prevImage();
        }
    };

    /**
     * Event handler for the play buton
     */
    View.OnClickListener playImgHandle = new View.OnClickListener(){
        public void onClick(View v){
            endOfPics = false;
            useHandler();
        }
    };

    /**
     * Event handler for the next image button
     */
    View.OnClickListener nextImgHandle = new View.OnClickListener(){
        public void onClick(View v){
            nextImage();
        }
    };

    /**
     * Display previous image
     */
    private void prevImage(){
        if(currentIndex - 1 >= 0){
            imgViewMorph.setImageBitmap(bmpArr[--currentIndex]);
        }
    }

    /**
     * Display next image
     */
    private void nextImage(){
        if(currentIndex + 1 < bmpArr.length){
            imgViewMorph.setImageBitmap(bmpArr[++currentIndex]);
        }else {
            endOfPics = true;
        }
    }
}
