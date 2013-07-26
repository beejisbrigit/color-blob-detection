package org.opencv.samples.colorblobdetect;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

public class ColorBlobDetectionView extends SampleCvViewBase implements OnTouchListener {

	private Mat mRgba;
	private Mat mRgbaLast = new Mat();
	
	private Bitmap mBmp;
	private int mCreateBmp = 0;
	private boolean displayStatus = false;
	private int frameCount = 0;

	private boolean mIsColorSelected = false;
	private Scalar mBlobColorRgba = new Scalar(255);
	private Scalar mBlobColorHsv = new Scalar(255);
	private ColorBlobDetector mDetector = new ColorBlobDetector();
	private Mat mSpectrum = new Mat();
	private static Size SPECTRUM_SIZE = new Size(200, 32);

	// Logcat tag
	private static final String TAG = "Example/ColorBlobDetection";
	
	private static final Scalar CONTOUR_COLOR = new Scalar(255,0,0,255);
	
	// for data collection
	private boolean mEnableDataCollection = false;
	private JSONArray mDataSet;
	
	public ColorBlobDetectionView(Context context)
	{
        super(context);
        setOnTouchListener(this);
	}
	
	
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            // initialize Mat before usage
            mRgba = new Mat();
        }
        
        super.surfaceCreated(holder);
    }
	
	public boolean onTouch(View v, MotionEvent event)
	{
		if(!mIsColorSelected)
		{
	        int cols = mRgba.cols();
	        int rows = mRgba.rows();
	        
	        int xOffset = (getWidth() - cols) / 2;
	        int yOffset = (getHeight() - rows) / 2;
	        
	        int x = (int)event.getX() - xOffset;
	        int y = (int)event.getY() - yOffset;
	        
	        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
	        
	        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
	  
	        Rect touchedRect = new Rect();
	        
	        int r = 10;
	        touchedRect.x = (x>r) ? x-r : 0;
	        touchedRect.y = (y>r) ? y-r : 0;
	
	        touchedRect.width = (x+r < cols) ? x + r - touchedRect.x : cols - touchedRect.x;
	        touchedRect.height = (y+r < rows) ? y + r - touchedRect.y : rows - touchedRect.y;
	       
	        /*touchedRect.x = (x>4) ? x-4 : 0;
	        touchedRect.y = (y>4) ? y-4 : 0;
	
	        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
	        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
	        */
	        	
	        Log.i(TAG, "Touch region ULHC: (" + touchedRect.x + ", " + touchedRect.y + ")");
	        Log.i(TAG, "Touch region LRHC: (" + (touchedRect.x+touchedRect.width) + ", " + (touchedRect.y + touchedRect.height)+ ")");
	        Mat touchedRegionRgba = mRgba.submat(touchedRect);
	        
	        //Mat touchedRegionHsv = touchedRegionRgba.clone();
	        Mat touchedRegionHsv = new Mat();
	        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
	              
	        // Calculate average color of touched region
	        // sum pixel values in each channel
	        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
	        int pointCount = touchedRect.width*touchedRect.height;
	        // get average for each channel (val.length = 3)
	        
	        for (int i = 0; i < mBlobColorHsv.val.length; i++)
	        {
	        	mBlobColorHsv.val[i] /= pointCount;
	        }
	        
	        //mBlobColorRgba = mBlobColorHsv;
	        // convert average HSV value to RGB (3 channel value)
	        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
	        
	        //Log.i(TAG, "Touched rgba color:" + mBlobColorRgba.val[0] );
	        
	        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] + 
	    			", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
	   		
	        // set threshold values for grayscale equivalent of touched target rgba color
	   		//mDetector.setHsvColor(mBlobColorHsv); // HSV test
	   		mDetector.setGreyscaleValue(mBlobColorRgba);
	   		
	   		//Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
	   		
	        mIsColorSelected = true;
	        
	        
		}
        
        return false; // don't need subsequent touch events
	}

	@Override
	protected Bitmap processFrame(VideoCapture capture) {
		
		// temporary
		Mat newResult = null;
		Mat hsvMat = null;
		Mat result = null;
		Boolean displayGreyImage = true;
		
		// make this true.
		mIsColorSelected = true;
		if (!mIsColorSelected)
		{
			capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);	// color image
			Core.circle(mRgba, new Point(500,500), 20, new Scalar(255, 0, 0, 255), 3);
			mBmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
			
		}
		else
		{
		
			// (1) capture first image
			capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_GREY_FRAME);	// grey image
					
			double scale = 0.12;
			//double scale = 0.4; // o.5 really fast!
			final Size imgSize = new Size(mRgba.cols()*scale, mRgba.rows()*scale);
			
			/*Mat */result = new Mat();
			Imgproc.resize(mRgba, result, imgSize);
			Mat mask = new Mat(result.size(), CvType.CV_8U);
			// COLOR THRESHOLDING
			// output mask needs to be 8UC
			///mDetector.createColorMask(result, result);
			////mBmp = Bitmap.createBitmap(result.cols(),result.rows(), Bitmap.Config.RGB_565);
			///Core.inRange(mask,new Scalar(0), new Scalar(100), result);
			
			// TODO: move this to ColorBlobDetector.java
			/// ---- original contour blob detect ------------
			boolean doContourDetect = true;
			
			if(doContourDetect)
			{
				
				Core.inRange(result, new Scalar(0), new Scalar(64), result);
				
				// Canny edge detection
				//Imgproc.Canny(result, edgeMask, 80, 100);
				//Imgproc.Canny(result, result, 80, 100);
				Imgproc.Canny(result, result, 200, 255);
				
				// AND results
				//Core.bitwise_and(result, mask, result);
				
				
				// ========= contours --------------
		        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		        Mat hierarchy = new Mat();
		        Mat resultCopy = result.clone();;
		        
		        // Detect contours
		        Imgproc.findContours(resultCopy, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	
		        // Find max contour area
		        List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
		        double maxArea = 0;
		        double mMinContourArea = 0.2;
		        double mMaxContourArea = 0.9;
		        Iterator<MatOfPoint> each = contours.iterator();
		        // look for contour with maximum area
		        while (each.hasNext())
		        {
		        	MatOfPoint wrapper = each.next();
		        	double area = Imgproc.contourArea(wrapper);
		        	if (area > maxArea)
		        		maxArea = area;
		        }
		        
		        // Filter contours by area and resize to fit the original image size
		        mContours.clear();
		        each = contours.iterator();
		        while (each.hasNext())
		        {
		        	MatOfPoint contour = each.next();
		       // 	if (Imgproc.contourArea(contour) > mMinContourArea*maxArea && Imgproc.contourArea(contour) < mMaxContourArea*maxArea)
		        	{
		        		// TODO: fix hard-coded upscale
		        		Core.multiply(contour, new Scalar(5,5), contour);
		        		mContours.add(contour);
		        	}
		        }
		        
		   
		        // ------- end contours ----------------
				
				final double upScale = 6;
				////final double upScale = 9;
				final Size greyImgSize = new Size(mRgba.cols()*upScale*scale, mRgba.rows()*upScale*scale); // downscale upscale 

				Point center = new Point(0,0);
				center = mDetector.getBlobCentroid(result);
		
				if(mEnableDataCollection == true)
				{
					try 
					{
						double xScale = upScale*center.x;
						double yScale = upScale*center.y;
						
						if(xScale != 0 || yScale != 0)
						{
							// TODO: current order: col, -row (x and y backwards!)
							this.addDataPoint(yScale, -xScale);	
						}
						//this.addDataPoint(5, 5);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Log.e(TAG, "Adding a data point throws a JSONException: " + e.getMessage());
					}
				}

				// HACKY
				//Imgproc.pyrUp(result, result);
		    	//Imgproc.pyrUp(result, result);
				
				// DISPLAY grey image
				if(displayGreyImage)
				{			 				
					Imgproc.resize(mRgba, mRgba, greyImgSize);
					////Imgproc.resize(mRgba, mRgba, imgSize);
					// draw circle or point
					Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_GRAY2RGB); // current frame
					//Core.circle(mRgba, new Point(mRgba.cols()/2, mRgba.rows()/2), 20, new Scalar(255, 0, 0, 255), 3);
					Core.circle(mRgba, new Point(upScale*center.y, upScale*center.x) , 20, new Scalar(255, 0, 0, 255), 3);
					////Core.circle(mRgba, new Point(center.y, center.x) , 8, new Scalar(255, 0, 0, 255), 3);
					
					// Add center-pendulum-box here.
					if(mEnableDataCollection == false)
					{
						final int boxSize = 100;
						final Point centerUL = new Point(mRgba.width()/2 - boxSize, mRgba.height()/2 - boxSize*0.5);
						final Point centerLR = new Point(mRgba.width()/2 + boxSize, mRgba.height() - 5);					
						Core.rectangle(mRgba,centerUL, centerLR, new Scalar(255, 0, 0, 255), 3);
						
						Core.putText(mRgba, new String("center pendulum somewhere in box"), new Point(mRgba.width()/2 - 3*boxSize, mRgba.height()/2 - boxSize), 
								2/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 2);
						//Core.circle(mRgba, new Point(20, mRgba.height() - 20) , 8, new Scalar(255, 0, 0, 255), -1);
						
					}
					else
					{
						
						if(displayStatus == true)
						{
							Core.putText(mRgba, new String("[COLLECTING DATA]"), new Point(0, mRgba.height() - 10 ), 
								1/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(0, 255, 0, 255), 2);
							//Core.circle(mRgba, new Point(20, mRgba.height() - 20) , 6, new Scalar(0, 255, 0, 255), -1);
							//displayStatus = false;
						}
						else
						{
							Core.putText(mRgba, new String("[COLLECTING DATA]"), new Point(0, mRgba.height() - 10 ), 
									1/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(0, 255, 0, 128), 2);
							//Core.circle(mRgba, new Point(10, mRgba.height() - 10) , 8, new Scalar(0, 255, 0, 255), -1);
								
							displayStatus = true;
						}
					}
					mBmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
				
				}
				// display threshold image.
				else // IDEA: draw circle on original image
				{
					//Log.i(TAG, "---------------------------RESULT img = " + result.toString());
					Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2RGB);
					Imgproc.resize(result, result, greyImgSize);
					
					// Draw centroid circle on edge image
					//Core.circle(mRgba, new Point(result.cols()/2, result.rows()/2), 20, new Scalar(255, 0, 0, 255), 3);
					/////Core.circle(result, new Point(upScale*center.y, upScale*center.x) , 10, new Scalar(255, 0, 0, 255), 3);
					//Core.circle(result, new Point(center.y, center.x), 0, new Scalar(255, 0, 0, 255), 3);
					
					/// Draw contours
					Imgproc.drawContours(result, mContours, -1, new Scalar(255,0,0,255), -10);
					//Imgproc.drawContours(result, mContours, -1, new Scalar(255,0,0,255), 10);
					mBmp = Bitmap.createBitmap(result.cols(),result.rows(), Bitmap.Config.RGB_565);
				}
	
				
			}
			else // use ColorBlobDetector.java
			{
				mDetector.processBrigit(result,result, result);
				mBmp = Bitmap.createBitmap(result.cols(),result.rows(), Bitmap.Config.RGB_565);
			}
			
		}
			
			
			/// ---- END original contour blob detect ------------
			
			

			// ---- for FRAME DIFF'ing
			//// ------- NORMAL processing ----------------
			// convert rgb images to CV_32F
/*			Mat a = mRgba.clone();
			Imgproc.resize(a,a,imgSize);
			a.convertTo(a, CvType.CV_32S); // current frame
			// resize image for faster processing (use pyr image?)
			
			Mat b = null;
			
			if(!mRgbaLast.empty())
			{
				b = mRgbaLast.clone();
				Imgproc.resize(b,b,imgSize);
				b.convertTo(b, CvType.CV_32S);	// last frame
			}
			else
			{
				b = a.clone(); // current frame
			}
			
			*/
		//// ------- end NORMAL processing ----------------
			
		/* WORKS
			Mat b = new Mat(mRgba.size(), CvType.CV_32S);
			
			if(!mRgbaLast.empty())
			{
				mRgbaLast.convertTo(b, CvType.CV_32S);	// last frame
			}
			else
			{
				mRgba.convertTo(b, CvType.CV_32S); // current frame
				
			}
		*/ // WORKS

			//// ------- NORMAL processing ----------------
			// result
			//Mat result = new Mat(a.size(), CvType.CV_32S);
			//// /*Mat */ result = new Mat(a.size(), CvType.CV_8U);
			// COLOR THRESHOLDING
			// output mask needs to be 8UC
			////mDetector.createColorMask(a, result);
			//mDetector.createColorMask(hsvMat, result);
	
			//Log.i(TAG, "---------------------------FIRST (a) img = " + a.toString());
			//Log.i(TAG, "---------------------------FIRST (a) img = " + a.dump());
			//Log.i(TAG, "---------------------------SECOND (b) img = " + b.toString());
			//Log.i(TAG, "---------------------------SECOND (b) img = " + b.dump());
		
			// FRAME DIFFERENCING
			//Core.absdiff(a,b, result); // alpha channel becomes 0 through subtraction for 3 channel image
			
			
			////Log.i(TAG, "---------------------------DIFF img = " + result.toString());
			//Log.i(TAG, "---------------------------DIFF img # channels = " + result.channels());
			//Log.i(TAG, "---------------------------DIFF img = " + result.dump());
		
			// Need to convert 32bit signed image to 8-bit unsigned to convert to bitmap
			/////*Mat */newResult = new Mat(a.size(), CvType.CV_8U);
			////result.convertTo(newResult, CvType.CV_8U);
			
			////Log.i(TAG, "---------------------------DIFF (after) img = " + newResult.toString());
			//Log.i(TAG, "--------------------------- DIFF (after) img = " + newResult.dump());
		
	    	// Apply dilation to mask
	    	//Mat resultCopy = newResult.clone();
	    	//Imgproc.dilate(resultCopy, newResult, new Mat());
			//Imgproc.erode(newResult, newResult, new Mat());
	    	//Imgproc.dilate(newResult, newResult, new Mat());
			// ----------------
			
			
			// FROM TEST (simplify): this copies the same exact image as in (1) mRgba 
			////capture.retrieve(mRgbaLast, Highgui.CV_CAP_ANDROID_GREY_FRAME);	// grey image
			//Imgproc.resize(mRgbaLast, mRgbaLast, imgSize);
			//Log.i(TAG, "---------------------------SECOND img = " + mRgbaLast.dump());
			//mRgbaLast = mRgba.clone();
			
		
			// RGB bitmap to export for display 
			//mBmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
			////mBmp = Bitmap.createBitmap(newResult.cols(), newResult.rows(), Bitmap.Config.RGB_565);
			//mBmp = Bitmap.createBitmap(result.cols(),result.rows(), Bitmap.Config.RGB_565);
			
		//// ------- end NORMAL processing ----------------
			
				
        // copy captured image to bmp
        try {
        	
        	if (!mIsColorSelected)
        	{
        		Utils.matToBitmap(mRgba, mBmp);			
        	}
        	
        	if (mIsColorSelected)
        	{
        		//Utils.matToBitmap(newResult, mBmp); // works!
        		//Utils.matToBitmap(result, mBmp); // greyscale test, works
        		if(displayGreyImage)
        			Utils.matToBitmap(mRgba, mBmp);
        		else
        			Utils.matToBitmap(result, mBmp); 
        	}
        } catch(Exception e) {
        	Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
            mBmp.recycle();
            mBmp = null;
            //mCreateBmp = 0;
        }
        
        
        return mBmp;
	}
	


	public void startDataCollection(JSONArray dataSet)
	{
		Log.i(TAG, "--------------- STARTING DATA COLLECTION ---------------");
		// Turn data collection on
		mEnableDataCollection = true;
		
		// let mDataSet point to dataSet set in ColorBlobDetectionActivity
		mDataSet = new JSONArray();
	}
	
	
	public JSONArray stopDataCollection()
	{
		Log.i(TAG, "--------------- STOPPING DATA COLLECTION ---------------");
		// Turn data collection off
		mEnableDataCollection = false;
		return mDataSet;
		
	}
	
	public Boolean dataCollectionEnabled()
	{
		return mEnableDataCollection;
	}
	
	private void addDataPoint(double x, double y) throws JSONException
	{	
		// HACKY test DATA
		float xy[] = new float[]{10.0f,10.0f};
		
		Log.i(TAG, "creating JSON data array");
		
		//while(x < 10)
		{
			JSONArray dataJSON = new JSONArray();
			
	        Calendar c = Calendar.getInstance();
	        
	        long currentTime =  (long) (c.getTimeInMillis() /*- 14400000*/);
	       
	        DecimalFormat toThou = new DecimalFormat("#,###,##0.000");
							
			/* Convert floating point to String to send data via HTML 
			/* Posn-x    */  dataJSON.put(x);//toThou.format(xy[0]));
			
			/* Posn-y    */  dataJSON.put(y);//toThou.format(xy[1]));

			/* Time       */ dataJSON.put(currentTime); 
											                 
			mDataSet.put(dataJSON);
			x++;
			//xy[0] += 2.0;
			//xy[1] += 2.0;
			Log.i(TAG, "--------------- ADDING DATA POINT ---------------");
			
		}
		// end HACKY test DATA
	}
	private Scalar converScalarHsv2Rgba(Scalar hsvColor)
	{	
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        
        return new Scalar(pointMatRgba.get(0, 0));
	}
	
    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();

            mRgba = null;
        }
    }
}
