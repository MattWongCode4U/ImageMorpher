package com.example.matthew.morpher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;

import java.util.ArrayList;

/**
 * DrawView class
 *
 * Describes views that hold the lines
 */
public class DrawView extends View {
    //class members
    private int radius = 30;
    private int lineWidth = 10;
    private Paint drawingPaint = new Paint();
    private Paint paint = new Paint();
    private Point start = new Point();
    private Point end = new Point();
    private ArrayList<Line> lineList = new ArrayList<>();
    private boolean drawTempFlag = false;
    public boolean hasLineSelected = false;

    /**
     * DrawView constructor
     * @param context Where to put the DrawView
     */
    public DrawView(Context context){
        super(context);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(lineWidth);

        drawingPaint.setColor(Color.BLUE);
        drawingPaint.setStrokeWidth(lineWidth);
    }

    @Override
    /**
     * What to do when the DrawView is supposed to be drawn
     * @param canvas Canvas to draw on that represents the DrawView
     */
    protected void onDraw(Canvas canvas) {
        if(drawTempFlag) {
            canvas.drawLine(start.x, start.y, end.x, end.y, drawingPaint);
        }
        for(Line l : lineList){
            if(l.pairSelection){
                paint.setColor(Color.YELLOW);
            } else {
                paint.setColor(Color.BLACK);
            }
            canvas.drawLine(l.getStart().x, l.getStart().y, l.getEnd().x, l.getEnd().y, paint);
            if(l.pairSelection && l.isSelected){
                selectedLine(l, canvas);
            }
        }
        //invalidate();
    }

    /**
     * Getter for the list of Lines
     * @return Returns the list of lines in this DrawView
     */
    public ArrayList<Line> getLineList(){
        return lineList;
    }

    /**
     * Adding a Line to the DrawView's list
     * @param line Line to add to the list of Lines
     */
    public void addLine(Line line){
        lineList.add(line);
    }

    /**
     * Allows for a temporary line to be shown while initially creating a line.
     * @param tLine Temporary line created when initializing the line
     */
    public void setTempLine(Line tLine){
        start.x = tLine.getStart().x;
        start.y = tLine.getStart().y;
        end.x = tLine.getEnd().x;
        end.y = tLine.getEnd().y;
    }

    /**
     * Remove the last line that was added
     */
    public void removeLastLine(){
        if(lineList.size() > 0){
            lineList.remove(lineList.size() - 1);
        }
        invalidate();
    }

    /**
     * Remove all the lines in the list
     */
    public void removeAllLines(){
        if(lineList.size() > 0){
            lineList.clear();
        }
        invalidate();
    }

    /**
     * Set flag for the outline selection
     */
    public void changeTempDrawFlag(){
        drawTempFlag = !drawTempFlag;
    }

    /**
     * Draw a line over the selected line and 2 circles at its end points
     * @param l Line that needs to be outlined
     * @param c Canvas to draw on
     */
    public void selectedLine(Line l, Canvas c){
        Paint paintEnds = new Paint();
        paintEnds.setColor(Color.GREEN);

        Point strPt = l.getStart();
        Point endPt = l.getEnd();

        c.drawCircle(strPt.x, strPt.y, radius, paintEnds);
        c.drawCircle(endPt.x, endPt.y, radius, paintEnds);
    }

    /**
     * Check if line was touched
     * @param touchPt Point that was touched
     * @param l Line to be checked
     * @return If the line was clicked on
     */
    public boolean isOnLine(Point touchPt, Line l){
        int sx = l.getStart().x;
        int sy = l.getStart().y;
        int ex = l.getEnd().x;
        int ey = l.getEnd().y;

        double tx = (((double)touchPt.x - (double)sx) / ((double)ex - (double)sx));
        double ty = (((double)touchPt.y - (double)sy) / ((double)ey - (double)sy));

        if((tx >= 0 && tx <= 1) && (ty >= 0 && ty <= 1)){
                return true;
        }
        return false;
    }

    /**
     * Check if touch event on the endpoint of a line
     * @param touchPt Point that was touched
     * @param l Line to be checked
     * @return If the end point or a radius around it was clicked on
     */
    public boolean isOnDot(Point touchPt, Line l){
        int sx = l.getStart().x;
        int sy = l.getStart().y;
        int ex = l.getEnd().x;
        int ey = l.getEnd().y;

        if((((touchPt.x >= sx - radius) && (touchPt.x <= sx + radius)) && ((touchPt.y >= sy - radius) && (touchPt.y <= sy + radius)))){
            System.out.println("start touched");
            l.startSelected = true;
            return true;
        }

        if((((touchPt.x >= ex - radius) && (touchPt.x <= ex + radius)) && ((touchPt.y >= ey - radius) && (touchPt.y <= ey + radius)))){
            System.out.println("end touched");
            l.endSelected = true;
            return true;
        }
        return false;
    }
}
