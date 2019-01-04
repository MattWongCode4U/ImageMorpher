package com.example.matthew.morpher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main Activity for the program
 */
public class MainActivity extends AppCompatActivity {
    //CONSTANTS
    final int PHOTO_ACCESS_CODE = 111;
    final int DRAW_MODE_FLAG = 0;
    final int EDIT_MODE_FLAG = 1;
    final int DEFAULT_INTERMEDIATE_FRAMES = 1;
    final double A = 0.01;
    final double P = 0;
    final double B = 2;
    final String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    //Declarations
    VectorProjection vp = new VectorProjection();
    ArrayList<Pair<Line, Line>> list = new ArrayList<>();
    Point start = new Point();
    Point end = new Point();
    DrawView drawView1;
    DrawView drawView2;
    Button morphButton;
    EditText frameNumEditText;
    String filePath;
    int id = 100;
    int permCode = 300;
    int imgSwitch = 0;
    int modeSwitch = 0;

    @Override
    /**
     * Occurs when the activity is created.
     * Sets up the layouts and event handlers.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_main);

        FrameLayout frame1 = (FrameLayout)findViewById(R.id.frame1);
        FrameLayout frame2 = (FrameLayout)findViewById(R.id.frame2);

        morphButton = (Button)findViewById(R.id.morphBtn);
        morphButton.setOnClickListener(morphHandle);

        Toolbar tb = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(tb);

        drawView1 = new DrawView(this);
        drawView2 = new DrawView(this);

        drawView1.setId(id + 0);
        drawView2.setId(id + 1);

        drawView1.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Point tEvent = new Point((int)event.getX(), (int)event.getY());
                if(tEvent.x < 0){
                    tEvent.x = 0;
                } else if(tEvent.x > drawView2.getWidth()){
                    tEvent.x = drawView2.getWidth();
                }

                if(tEvent.y < 0){
                    tEvent.y = 0;
                } else if(tEvent.y > drawView2.getHeight()){
                    tEvent.y = drawView2.getHeight();
                }
                //Draw mode
                if(modeSwitch == DRAW_MODE_FLAG) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            start.set(tEvent.x, tEvent.y);
                            end.set(tEvent.x, tEvent.y);
                            Line teLine = new Line(start, end);
                            drawView1.setTempLine(teLine);
                            drawView2.setTempLine(teLine);
                            drawView1.changeTempDrawFlag();
                            drawView2.changeTempDrawFlag();
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            end.set(tEvent.x, tEvent.y);
                            Line tLine = new Line(start, end);
                            drawView1.setTempLine(tLine);
                            drawView2.setTempLine(tLine);
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            end.set(tEvent.x, tEvent.y);

                            Line tempLine1 = new Line(new Point(start.x, start.y), new Point(end.x, end.y));
                            Line tempLine2 = new Line(new Point(start.x, start.y), new Point(end.x, end.y));
                            Pair<Line, Line> pairLine = new Pair<>(tempLine1, tempLine2);

                            drawView1.addLine(tempLine1);
                            drawView2.addLine(tempLine2);
                            list.add(pairLine);
                            drawView1.changeTempDrawFlag();
                            drawView2.changeTempDrawFlag();
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                    }
                //Edit mode
                } else if(modeSwitch == EDIT_MODE_FLAG){
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            System.out.println("X: " + (int)event.getX() + " Y: " + (int)event.getY());
                            for(Pair<Line, Line> p : list) {
                                if(p.first.isSelected && drawView1.isOnDot(tEvent, p.first)){
                                    System.out.println("Endpoint clicked");
                                } else {

                                    if (drawView1.isOnLine(new Point(tEvent.x, tEvent.y), p.first)) {
                                        System.out.println("on Line dv1");
                                        if(drawView1.hasLineSelected){
                                            System.out.println("in1");
                                            for(Line l: drawView1.getLineList()){
                                                l.isSelected = false;
                                                l.pairSelection = false;
                                            }
                                            for(Line l: drawView2.getLineList()){
                                                l.isSelected = false;
                                                l.pairSelection = false;
                                            }
                                        }
                                        p.first.isSelected = true;
                                        p.first.pairSelection = true;
                                        p.second.pairSelection = true;
                                        drawView1.hasLineSelected = true;
                                    } else {
                                        p.first.isSelected = false;
                                        p.first.pairSelection = false;
                                        p.second.pairSelection = false;
                                        drawView1.hasLineSelected = false;
                                    }
                                }
                            }
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            for(Line l: drawView1.getLineList()){
                                if(l.startSelected){
                                    //move startPt
                                    l.getStart().x = tEvent.x;
                                    l.getStart().y = tEvent.y;
                                }else if(l.endSelected){
                                    //move endPt
                                    l.getEnd().x = tEvent.x;
                                    l.getEnd().y = tEvent.y;
                                }
                            }
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            for(Line l: drawView1.getLineList()){
                                l.startSelected = false;
                                l.endSelected = false;
                            }
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                    }
                }
                return true;
            }
        });

        drawView2.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Point tEvent = new Point((int)event.getX(), (int)event.getY());
                if(tEvent.x < 0){
                    tEvent.x = 0;
                } else if(tEvent.x > drawView2.getWidth()){
                    tEvent.x = drawView2.getWidth();
                }

                if(tEvent.y < 0){
                    tEvent.y = 0;
                } else if(tEvent.y > drawView2.getHeight()){
                    tEvent.y = drawView2.getHeight();
                }
                //Draw mode
                if(modeSwitch == DRAW_MODE_FLAG) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            start.set(tEvent.x, tEvent.y);
                            end.set(tEvent.x, tEvent.y);
                            Line teLine = new Line(start, end);
                            drawView1.setTempLine(teLine);
                            drawView2.setTempLine(teLine);
                            drawView1.changeTempDrawFlag();
                            drawView2.changeTempDrawFlag();
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            end.set(tEvent.x, tEvent.y);
                            Line tLine = new Line(start, end);
                            drawView1.setTempLine(tLine);
                            drawView2.setTempLine(tLine);
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            end.set(tEvent.x, tEvent.y);

                            Line tempLine1 = new Line(new Point(start.x, start.y), new Point(end.x, end.y));
                            Line tempLine2 = new Line(new Point(start.x, start.y), new Point(end.x, end.y));
                            Pair<Line, Line> pairLine = new Pair<>(tempLine1, tempLine2);

                            drawView1.addLine(tempLine1);
                            drawView2.addLine(tempLine2);
                            list.add(pairLine);
                            drawView1.changeTempDrawFlag();
                            drawView2.changeTempDrawFlag();
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                    }
                //Edit mode
                } else if(modeSwitch == EDIT_MODE_FLAG){
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            System.out.println("X: " + (int)event.getX() + " Y: " + (int)event.getY());
                            for(Pair<Line, Line> p : list) {
                                if(p.second.isSelected && drawView2.isOnDot(tEvent, p.second)){
                                    System.out.println("Endpoint clicked");
                                } else {

                                    if (drawView2.isOnLine(new Point(tEvent.x, tEvent.y), p.second)) {
                                        System.out.println("on Line dv2");
                                        if(drawView2.hasLineSelected){
                                            System.out.println("in2");
                                            for(Line l: drawView1.getLineList()){
                                                l.isSelected = false;
                                                l.pairSelection = false;
                                            }
                                            for(Line l: drawView2.getLineList()){
                                                l.isSelected = false;
                                                l.pairSelection = false;
                                            }
                                        }
                                        p.second.isSelected = true;
                                        p.first.pairSelection = true;
                                        p.second.pairSelection = true;
                                        drawView2.hasLineSelected = true;
                                    } else {
                                        p.second.isSelected = false;
                                        p.first.pairSelection = false;
                                        p.second.pairSelection = false;
                                        drawView2.hasLineSelected = false;
                                    }
                                }
                            }
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            for(Line l: drawView2.getLineList()){
                                if(l.startSelected){
                                    //move startPt
                                    l.getStart().x = tEvent.x;
                                    l.getStart().y = tEvent.y;
                                }else if(l.endSelected){
                                    //move endPt
                                    l.getEnd().x = tEvent.x;
                                    l.getEnd().y = tEvent.y;
                                }
                            }
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            for(Line l: drawView2.getLineList()){
                                l.startSelected = false;
                                l.endSelected = false;
                            }
                            drawView1.invalidate();
                            drawView2.invalidate();
                            break;

                    }
                }
                return true;
            }
        });

        frame1.addView(drawView1);
        frame2.addView(drawView2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item1MenuItem:
                imgSwitch = 1;
                getPic();
                return true;

            case R.id.item2MenuItem:
                imgSwitch = 2;
                getPic();
                return true;

            case R.id.deleteMenuItem:
                if(!list.isEmpty()) {
                    removeLastLine();
                    Toast delToast = Toast.makeText(this, "Last line deleted", Toast.LENGTH_SHORT);
                    delToast.show();
                }
                return true;

            case R.id.deleteAllMenuItem:
                if(!list.isEmpty()) {
                    removeAllLines();
                    Toast delAllToast = Toast.makeText(this, "All lines deleted", Toast.LENGTH_SHORT);
                    delAllToast.show();
                }
                return true;

            case R.id.drawMenuItem:
                modeSwitch = 0;
                item.setChecked(true);
                Toast drawToast = Toast.makeText(this, "Draw Mode", Toast.LENGTH_SHORT);
                drawToast.show();
                return true;

            case R.id.editMenuItem:
                modeSwitch = 1;
                item.setChecked(true);
                Toast editToast = Toast.makeText(this, "Edit Mode", Toast.LENGTH_SHORT);
                editToast.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Morph Button event
     * What happens when the Morph button is pressed
     */
    View.OnClickListener morphHandle = new View.OnClickListener(){
        public void onClick(View v){

            //check text box
            frameNumEditText = (EditText)findViewById(R.id.editTextFrameNum);
            String frames = frameNumEditText.getText().toString();
            int frameNum;
            if(frames.equals("")){
                frameNum = DEFAULT_INTERMEDIATE_FRAMES;
                frameNumEditText.setText(Integer.toString(frameNum));
            } else {
                frameNum = Integer.parseInt(frames);
            }

            //check images are set
            if(!image1isSet() && !image2isSet()){
                loadDefaultImages();
            }

            ImageView imgView = (ImageView)findViewById(R.id.imageView1);
            Bitmap bitmapStart = ((BitmapDrawable)imgView.getDrawable()).getBitmap();

            ImageView imgView2 = (ImageView)findViewById(R.id.imageView2);
            Bitmap bitmapEnd = ((BitmapDrawable)imgView2.getDrawable()).getBitmap();
            int i = 0;

            Intent intent = new Intent(getApplicationContext(), PicDisplayActivity.class);
            intent.putExtra("frameNum",frameNum);

            //send first image sent to other activity
            FileOutputStream fos;
            try{
                fos = openFileOutput("image" + i, Context.MODE_PRIVATE);
                bitmapStart.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                System.out.println("created first");
            } catch (IOException e){
                e.printStackTrace();
            }

            i++;

            //intermediate frames sent to other activity
            if(list.size() > 0) {
                Bitmap[] bitmapInter = createInterBitMapArr(frameNum);
                Bitmap[] bitmapInterRev = createInterBitMapArrRev(frameNum);
                Bitmap[] bitmapCrossDisolved = crossDisolve(bitmapInter, bitmapInterRev);
                FileOutputStream fosInter;
                for (int j = 0; j < bitmapCrossDisolved.length; i++, j++) {
                    try {
                        fosInter = openFileOutput("image" + i, Context.MODE_PRIVATE);
                        bitmapCrossDisolved[j].compress(Bitmap.CompressFormat.JPEG, 100, fosInter);
                        fosInter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            //send last image sent to other activity
            FileOutputStream fosLast;
            try{
                fosLast = openFileOutput("image" + i, Context.MODE_PRIVATE);
                bitmapEnd.compress(Bitmap.CompressFormat.JPEG, 100, fosLast);
                fosLast.close();
            } catch (IOException e){
                e.printStackTrace();
            }

            //open picture activity
            startActivity(intent);
        }
    };

    /**
     * Load images if no images are set
     */
    private void loadDefaultImages(){
        ImageView img1 = (ImageView) findViewById(R.id.imageView1);
        img1.setImageResource(R.drawable.nomad1);

        greyscale();
    }

    /**
     * Change a picture to a greyscale version
     */
    private void greyscale(){
        ImageView imgView = (ImageView)findViewById(R.id.imageView1);
        ImageView imgView2 = (ImageView)findViewById(R.id.imageView2);
        Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        for(int x = 0; x < bitmap.getWidth(); x++){
            for(int y = 0; y < bitmap.getHeight(); y++){

                int pixel = bitmap.getPixel(x, y);

                int grey = (Color.red(pixel) + Color.blue(pixel) + Color.green(pixel)) / 3;
                int newPixel = Color.rgb(grey, grey, grey);

                bitmap2.setPixel(x, y, newPixel);
            }
        }
        imgView2.setImageBitmap(bitmap2);
    }

    /**
     * Create prompt to get pictures.
     * Android 6.0 requirement to request permission to access pictures
     */
    private void getPic(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        requestPermissions(perms, permCode);

        if(checkSelfPermission(perms[0]) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(perms[1]) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(intent, PHOTO_ACCESS_CODE);
        }
    }

    /**
     * What to do when the picture prompt finishes
     * @param requestCode Code for the request to handle
     * @param resultCode Resulting code that was received when the activity finished
     * @param data Data received from the activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_ACCESS_CODE && resultCode == RESULT_OK && data != null){
            Uri image = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(image,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            filePath = new String(picturePath);
            System.out.println(filePath);
            if(imgSwitch == 1) {
                Bitmap b = BitmapFactory.decodeFile(picturePath);
                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                b = Bitmap.createScaledBitmap(b, (int)(0.3 * b.getWidth()), (int)(0.3 * b.getHeight()), true);
                imageView.setImageBitmap(b);

                if(image2isSet()){
                    ImageView img2 = (ImageView) findViewById(R.id.imageView2);
                    Bitmap b2 = ((BitmapDrawable)img2.getDrawable()).getBitmap();
                    double wRatio = (double)b.getWidth() / b2.getWidth();
                    double hRatio = (double)b.getHeight() / b2.getHeight();

                    b2 = Bitmap.createScaledBitmap(b2, (int)Math.round(b2.getWidth() * wRatio), (int)Math.round(b2.getHeight() * hRatio), true);
                    img2.setImageBitmap(b2);
                }

            } else if(imgSwitch == 2){
                Bitmap b = BitmapFactory.decodeFile(picturePath);
                ImageView imageView = (ImageView) findViewById(R.id.imageView2);
                b = Bitmap.createScaledBitmap(b, (int)(0.3 * b.getWidth()), (int)(0.3 * b.getHeight()), true);

                if(image1isSet()){
                    ImageView img1 = (ImageView) findViewById(R.id.imageView1);
                    Bitmap b1 = ((BitmapDrawable)img1.getDrawable()).getBitmap();
                    double wRatio = (double)b1.getWidth() / b.getWidth();
                    double hRatio = (double)b1.getHeight() / b.getHeight();

                    b = Bitmap.createScaledBitmap(b, (int)Math.round(b.getWidth() * wRatio), (int)Math.round(b.getHeight() * hRatio), true);
                }

                imageView.setImageBitmap(b);
            }
        }
    }

    /**
     * Check if there is an image set in the first image view
     * @return true if picture 1 has a picture in it, false otherwise
     */
    private boolean image1isSet(){
        ImageView img = (ImageView) findViewById(R.id.imageView1);
        if(img.getDrawable() == null){
            return false;
        }
        return true;
    }

    /**
     * Check if there is an image set in the second image view
     * @return true if picture 2 has a picture in it, false otherwise
     */
    private boolean image2isSet(){
        ImageView img = (ImageView) findViewById(R.id.imageView2);
        if(img.getDrawable() == null){
            return false;
        }
        return true;
    }

    /**
     * Remove the last line that was added
     */
    public void removeLastLine(){
        DrawView dv1 = (DrawView) findViewById(id + 0);
        DrawView dv2 = (DrawView) findViewById(id + 1);
        dv1.removeLastLine();
        dv2.removeLastLine();
        if(list.size() > 0){
            list.remove(list.size() - 1);
        }
    }

    /**
     * Remove all the lines that were drawn
     */
    public void removeAllLines(){
        DrawView dv1 = (DrawView) findViewById(id + 0);
        DrawView dv2 = (DrawView) findViewById(id + 1);
        dv1.removeAllLines();
        dv2.removeAllLines();
        if(list.size() > 0){
            list.clear();
        }
    }

    /**
     * Creates a bitmap array of intermediate frames with the left picture as a source
     * @param interFrames amount of intermediate frames to produce
     * @return array of bitmaps of intermediate frames
     */
    public Bitmap[] createInterBitMapArr(int interFrames){
        ImageView imgView = (ImageView)findViewById(R.id.imageView1);
        Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        Bitmap[] result = new Bitmap[interFrames];

        //for every intermediate frame
        for(int i = 0; i < interFrames; i++) {

            Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            double t = (double)(interFrames - i) / (interFrames + 1);

            //for each pixel
            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {

                    int lineNum = 0;
                    double[] weights = new double[list.size()];
                    double[] deltaX = new double[list.size()];
                    double[] deltaY = new double[list.size()];

                    //int pixel = bitmap.getPixel(x, y);
                    int newX = x;
                    int newY = y;

                    Point pointX = new Point(x, y);

                    //for each pair of lines
                    for(Pair<Line, Line> pair: list) {
                        Point ptP = new Point(pair.second.getStart().x, pair.second.getStart().y);
                        Point ptQ = new Point(pair.second.getEnd().x, pair.second.getEnd().y);

                        psVector vectorPQ = new psVector(ptQ, ptP);

                        psVector PQnorm = vp.normalVector(vectorPQ);

                        double xPPrDif = pair.second.getStart().x - pair.first.getStart().x;
                        double yPPrDif = pair.second.getStart().y - pair.first.getStart().y;
                        double xQPrDif = pair.second.getEnd().x - pair.first.getEnd().x;
                        double yQPrDif = pair.second.getEnd().y - pair.first.getEnd().y;

                        Point ptPPrime = new Point((int)(pair.first.getStart().x + (t * xPPrDif)), (int)(pair.first.getStart().y + (t * yPPrDif)));
                        Point ptQPrime = new Point((int)(pair.first.getEnd().x + (t * xQPrDif)), (int)(pair.first.getEnd().y + (t * yQPrDif)));

                        psVector vectorPQprime = new psVector(ptQPrime, ptPPrime);

                        psVector PQprimeNorm = vp.normalVector(vectorPQprime);

                        psVector vectorXP = new psVector(ptP, pointX);
                        psVector vectorPX = new psVector(pointX, ptP);

                        double d = vp.projectionLength(vectorXP, PQnorm);

                        double frac = vp.projectionLength(vectorPX, vectorPQ);
                        double fracPercent = vp.getFractionPercent(vectorPQ, frac);

                        //frac% * |PQ|
                        psVector percentAlongPrime = new psVector(fracPercent * vectorPQprime.getX(), fracPercent * vectorPQprime.getY());

                        //d * n/|n|
                        psVector distNormDir = new psVector(d * PQprimeNorm.getX() / PQprimeNorm.getLength(), d * PQprimeNorm.getY() / PQprimeNorm.getLength());

                        //x'
                        Point pointXprime = new Point((int)(ptPPrime.x + percentAlongPrime.getX() - distNormDir.getX()), (int)(ptPPrime.y + percentAlongPrime.getY() - distNormDir.getY()));

                        newX = pointXprime.x;
                        newY = pointXprime.y;

                        if(fracPercent < 0){
                            double newD = Math.sqrt((Math.pow(newX - ptP.x,2))+(Math.pow(newY - ptP.y,2)));
                            weights[lineNum] = vp.calculateWeight(vectorPQprime.getLength(), A, Math.abs(newD), P, B);
                        }else if(fracPercent > 1){
                            double newD = Math.sqrt((Math.pow(newX - ptQ.x,2))+(Math.pow(newY - ptQ.y,2)));
                            weights[lineNum] = vp.calculateWeight(vectorPQprime.getLength(), A, Math.abs(newD), P, B);
                        }else {
                            weights[lineNum] = vp.calculateWeight(vectorPQprime.getLength(), A, Math.abs(d), P, B);
                        }

                        deltaX[lineNum] = pointXprime.x - pointX.x;
                        deltaY[lineNum] = pointXprime.y - pointX.y;

                        lineNum++;
                    }//every line iterated through

                    //Sum weights
                    double totalWeight = 0;
                    for(int j = 0; j < list.size(); j++){
                        totalWeight += weights[j];
                    }

                    //Weight * Deltas
                    double total_WeightTimesDeltaX = 0;
                    double total_WeightTimesDeltaY = 0;
                    for(int j = 0; j < list.size(); j++){
                        total_WeightTimesDeltaX += weights[j] * deltaX[j];
                        total_WeightTimesDeltaY += weights[j] * deltaY[j];
                    }

                    //Avg Delta
                    double avgDeltaX = total_WeightTimesDeltaX / totalWeight;
                    double avgDeltaY = total_WeightTimesDeltaY / totalWeight;

                    newX = (int)(x + avgDeltaX);
                    newY = (int)(y + avgDeltaY);

                    //Boundary check on X
                    while(newX < 0){
                        newX = ((bitmap.getWidth() - 1) + newX);
                    }
                    while(newX >= bitmap.getWidth()){
                        newX = (newX - (bitmap.getWidth() - 1));
                    }

                    //Boundary check on Y
                    while(newY < 0){
                        newY = ((bitmap.getHeight() - 1) + newY);
                    }
                    while(newY >= bitmap.getHeight()){
                        newY = (newY - (bitmap.getHeight() - 1));
                    }

                    int newPixel = bitmap.getPixel(newX,newY);

                    newBitmap.setPixel(x, y, newPixel);
                }
            }//every pixel iterated through in one bitmap

            result[i] = newBitmap;

        }//every bitmap frame iterated through
        return result;
    }

    /**
     * Creates a bitmap array of intermediate frames with the right picture as a source
     * @param interFrames amount of intermediate frames to produce
     * @return array of bitmaps of intermediate frames
     */
    public Bitmap[] createInterBitMapArrRev(int interFrames){
        ImageView imgView = (ImageView)findViewById(R.id.imageView2);
        Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        Bitmap[] result = new Bitmap[interFrames];

        //for every intermediate frame
        for(int i = 0; i < interFrames; i++) {

            Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            boolean printFirstTime = true;
            double t = (double)(i + 1) / (interFrames + 1);

            //for each pixel
            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {

                    int lineNum = 0;
                    double[] weights = new double[list.size()];
                    double[] deltaX = new double[list.size()];
                    double[] deltaY = new double[list.size()];

                    //int pixel = bitmap.getPixel(x, y);
                    int newX = x;
                    int newY = y;

                    Point pointX = new Point(x, y);

                    //for each pair of lines
                    for(Pair<Line, Line> pair: list) {
                        Point ptP = new Point(pair.first.getStart().x, pair.first.getStart().y);
                        Point ptQ = new Point(pair.first.getEnd().x, pair.first.getEnd().y);

                        psVector vectorPQ = new psVector(ptQ, ptP);

                        psVector PQnorm = vp.normalVector(vectorPQ);

                        double xPPrDif = pair.first.getStart().x - pair.second.getStart().x;
                        double yPPrDif = pair.first.getStart().y - pair.second.getStart().y;
                        double xQPrDif = pair.first.getEnd().x - pair.second.getEnd().x;
                        double yQPrDif = pair.first.getEnd().y - pair.second.getEnd().y;

                        Point ptPPrime = new Point((int)(pair.second.getStart().x + (t * xPPrDif)), (int)(pair.second.getStart().y + (t * yPPrDif)));
                        Point ptQPrime = new Point((int)(pair.second.getEnd().x + (t * xQPrDif)), (int)(pair.second.getEnd().y + (t * yQPrDif)));

                        psVector vectorPQprime = new psVector(ptQPrime, ptPPrime);

                        psVector PQprimeNorm = vp.normalVector(vectorPQprime);

                        psVector vectorXP = new psVector(ptP, pointX);
                        psVector vectorPX = new psVector(pointX, ptP);

                        double d = vp.projectionLength(vectorXP, PQnorm);

                        double frac = vp.projectionLength(vectorPX, vectorPQ);
                        double fracPercent = vp.getFractionPercent(vectorPQ, frac);

                        //frac% * |PQ|
                        psVector percentAlongPrime = new psVector(fracPercent * vectorPQprime.getX(), fracPercent * vectorPQprime.getY());

                        //d * n/|n|
                        psVector distNormDir = new psVector(d * PQprimeNorm.getX() / PQprimeNorm.getLength(), d * PQprimeNorm.getY() / PQprimeNorm.getLength());

                        //x'
                        Point pointXprime = new Point((int)(ptPPrime.x + percentAlongPrime.getX() - distNormDir.getX()), (int)(ptPPrime.y + percentAlongPrime.getY() - distNormDir.getY()));

                        newX = pointXprime.x;
                        newY = pointXprime.y;

                        if(fracPercent < 0){
                            double newD = Math.sqrt((Math.pow(newX - ptP.x,2))+(Math.pow(newY - ptP.y,2)));
                            weights[lineNum] = vp.calculateWeight(vectorPQprime.getLength(), A, Math.abs(newD), P, B);
                        }else if(fracPercent > 1){
                            double newD = Math.sqrt((Math.pow(newX - ptQ.x,2))+(Math.pow(newY - ptQ.y,2)));
                            weights[lineNum] = vp.calculateWeight(vectorPQprime.getLength(), A, Math.abs(newD), P, B);
                        }else {
                            weights[lineNum] = vp.calculateWeight(vectorPQprime.getLength(), A, Math.abs(d), P, B);
                        }

                        deltaX[lineNum] = pointXprime.x - pointX.x;
                        deltaY[lineNum] = pointXprime.y - pointX.y;

                        lineNum++;
                    }//every line iterated through

                    //Sum weights
                    double totalWeight = 0;
                    for(int j = 0; j < list.size(); j++){
                        totalWeight += weights[j];
                    }

                    //Weight * Deltas
                    double total_WeightTimesDeltaX = 0;
                    double total_WeightTimesDeltaY = 0;
                    for(int j = 0; j < list.size(); j++){
                        total_WeightTimesDeltaX += weights[j] * deltaX[j];
                        total_WeightTimesDeltaY += weights[j] * deltaY[j];
                    }

                    //Avg Delta
                    double avgDeltaX = total_WeightTimesDeltaX / totalWeight;
                    double avgDeltaY = total_WeightTimesDeltaY / totalWeight;

                    newX = (int)(x + avgDeltaX);
                    newY = (int)(y + avgDeltaY);

                    //Boundary check on X
                    while(newX < 0){
                        newX = ((bitmap.getWidth() - 1) + newX);
                    }
                    while(newX >= bitmap.getWidth()){
                        newX = (newX - (bitmap.getWidth() - 1));
                    }

                    //Boundary check on Y
                    while(newY < 0){
                        newY = ((bitmap.getHeight() - 1) + newY);
                    }
                    while(newY >= bitmap.getHeight()){
                        newY = (newY - (bitmap.getHeight() - 1));
                    }

                    int newPixel = bitmap.getPixel(newX,newY);

                    newBitmap.setPixel(x, y, newPixel);
                }
            }//every pixel iterated through in one bitmap

            result[i] = newBitmap;

        }//every bitmap frame iterated through
        return result;
    }

    /**
     * Perform cross disolving given 2 arrays of bitmaps
     * @param fwd bitmap array in the forward direction
     * @param rev bitmap array in the reverse direction
     * @return resulting bitmap array after cross disolving
     */
    public Bitmap[] crossDisolve(Bitmap[] fwd, Bitmap[] rev){
        Bitmap[] result = new Bitmap[rev.length];
        for(int i = 0; i < fwd.length; i++){
            Bitmap tmp = Bitmap.createBitmap(fwd[i].getWidth(), fwd[i].getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap fwdtmp = fwd[i];
            Bitmap revtmp = rev[i];
            double fwdfrac = (double)(fwd.length - i) / (fwd.length + 1);
            double revfrac = (double)(i + 1) / (rev.length + 1);

            for(int x = 0; x < tmp.getWidth(); x++){
                for(int y = 0; y < tmp.getHeight(); y++){
                    int fwdPixel = (fwdtmp.getPixel(x,y));
                    int revPixel = (revtmp.getPixel(x,y));

                    int fwdRed = Color.red(fwdPixel);
                    int fwdGrn = Color.green(fwdPixel);
                    int fwdBlu = Color.blue(fwdPixel);
                    int revRed = Color.red(revPixel);
                    int revGrn = Color.green(revPixel);
                    int revBlu = Color.blue(revPixel);
                    int newPixel = Color.rgb((int)(fwdfrac*fwdRed + revfrac*revRed), (int)(fwdfrac*fwdGrn + revfrac*revGrn), (int)(fwdfrac*fwdBlu + revfrac*revBlu));
                    tmp.setPixel(x, y, newPixel);
                }
            }
            result[i] = tmp;
        }

        return result;
    }
}
