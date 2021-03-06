package org.opencv.samples.colorblobdetect;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Point3;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;
import org.opencv.*;

import android.util.Log;


public class ColorBlobDetector
{
	public void setColorRadius(Scalar radius)
	{
		mColorRadius = radius;
	}
	
	public void setGreyscaleValue(Scalar rgbColor)
	{
		// convert average RGB color to greyscale
		mGreyscale = convertScalarRgba2Grey(rgbColor);
		
		// set upper and lower bounds for single (first) channel only
	    	double minGrey = (mGreyscale.val[0] >= mColorRadius.val[0]) ? mGreyscale.val[0]-mColorRadius.val[0] : 0; 
    		double maxGrey = (mGreyscale.val[0]+mColorRadius.val[0] <= 255) ? mGreyscale.val[0]+mColorRadius.val[0] : 255;

  		mLowerBound.val[0] = minGrey;
   		mUpperBound.val[0] = maxGrey;
   		
   		Log.i(TAG, "--------------------------- Grey = " + mGreyscale);
   		Log.i(TAG, "--------------------------- minGrey = " + mLowerBound.val[0]);
		Log.i(TAG, "--------------------------- maxGrey = " + mUpperBound.val[0]);
		/*
	    double minR = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0; 
    	double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

  		mLowerBound.val[0] = minH;
   		mUpperBound.val[0] = maxH;

  		mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
   		mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

  		mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
   		mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

   		mLowerBound.val[3] = 0;
   		mUpperBound.val[3] = 255;

   		Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

 		for (int j = 0; j < maxH-minH; j++)
   		{
   			byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
   			spectrumHsv.put(0, j, tmp);
   		}

   		Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
   		*/

	}
	
	private Scalar convertScalarRgba2Grey(Scalar rgbColor)
	{	
        //Mat pointMatRgba = new Mat();
        //Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
	Mat pointMatGray = new Mat(1, 1, CvType.CV_8UC1); // single grayscale scalar value
        Mat pointMatRgba = new Mat(1, 1, CvType.CV_8UC3, rgbColor);
        //Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        Imgproc.cvtColor(pointMatRgba, pointMatGray, Imgproc.COLOR_RGBA2GRAY, 0);
        
        return new Scalar(pointMatGray.get(0, 0));
	}
	
	

	public void setHsvColor(Scalar hsvColor)
	{
	    	double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0; 
		double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

		mLowerBound.val[0] = minH;
   		mUpperBound.val[0] = maxH;

  		mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
   		mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

  		mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
   		mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

   		mLowerBound.val[3] = 0;
   		mUpperBound.val[3] = 255;

   		Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

 		for (int j = 0; j < maxH-minH; j++)
   		{
   			byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
   			spectrumHsv.put(0, j, tmp);
   		}

   		Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);

	}
	
	public Mat getSpectrum()
	{
		return mSpectrum;
	}
	
	public void setMinContourArea(double area)
	{
		mMinContourArea = area;
	}
	
	public void createColorMask(Mat image, Mat colorMask)
	{
    		// Create binary mask by thresholding the upper and lower bound 
    		// of touched area (cool!)
    		//Mat Mask = new Mat();
    		Core.inRange(image, mLowerBound, mUpperBound, colorMask);
    		// Apply dilation to mask
    		//Mat dilatedMask = new Mat();
    		//Imgproc.dilate(Mask, dilatedMask, new Mat());
		
	}
	
	public Point getBlobCentroid(Mat mask)
	{
		// Assume binary blob mask
		//Point center = new Point(0,0);
		Mat centers = new Mat();
		int cx = 0, cy = 0;
		int numPts = 0;
		int numRows = mask.rows();
		int numCols = mask.cols();

		double[] val = new double[1];
		
		// Initialize an array of x,y Scalars to represent a grid
		for(int i=0; i<numRows; i = i + 4) // y
		{	
			for(int j=0; j<numCols; j = j + 4) // x
			{
				
				val = mask.get(i,j);
				//Log.i(TAG, "i/j (row/col) = " + i + " " + j + " " + val);
				
				if(val[0] > 0)
				{ 
					cx += j; // cols
					cy += i; // row
					numPts++;
				}		
			}
		}
		
		if(numPts > 0)
		{
			cx /= numPts;
			cy /= numPts;
		}
		
		// get centroid via k-mmeans clustering
		//Core.kmeans(data, K, bestLabels, criteria, attempts, flags, centers)
		//float radius = 0;
		//imgproc.minEnclosingCircle(mask., center, radius);
		
		return new Point(cy, cx);
	}
	
	public void processBrigit(Mat rgbaImage, Mat rgbaImageLast, Mat result)
	{
		////Mat greyImage = new Mat();
		////greyImage = rgbaImage.clone();
		
		//double scale = 0.5;
		//final Size imgSize = new Size(rgbaImage.cols()*scale, rgbaImage.rows()*scale);
		////Imgproc.resize(greyImage, greyImage, imgSize);
		//Imgproc.resize(rgbaImage, rgbaImage, imgSize);
		
		boolean doHough = true;
		boolean doEdge = false;
		boolean doFrameDiff = false;
		
		if(doFrameDiff)
		{	
			// convert rgb images to CV_32F
			Mat a = new Mat();
			rgbaImage.convertTo(a, CvType.CV_32S);
			Mat b = new Mat();
			rgbaImageLast.convertTo(b, CvType.CV_32S);
			Core.add(a, new Scalar(1), a);
			// result
			Mat r = new Mat();
			Log.i(TAG, "---------------------------FIRST img = " + a.toString());
			Log.i(TAG, "---------------------------FIRST img = " + a.dump());
			Log.i(TAG, "---------------------------SECOND img = " + b.toString());
			Log.i(TAG, "---------------------------SECOND img = " + b.dump());
			Core.absdiff(a,b, r); // alpha channel becomes 0 through subtraction
			
			Log.i(TAG, "---------------------------DIFF img = " + r.toString());
			Log.i(TAG, "---------------------------DIFF img = " + r.dump());
			
		/*	
			Log.i(TAG, "--------------------------- orig FIRST img = " + rgbaImage.toString());
			Log.i(TAG, "--------------------------- orig SECOND img = " + rgbaImageLast.toString());
			Log.i(TAG, "--------------------------- orig FIRST img = " + rgbaImage.dump());
			Log.i(TAG, "--------------------------- orig SECOND img = " + rgbaImageLast.dump());
			
		*/
				/*		Log.i(TAG, "---------------------------FIRST img = " + rgbaImage.toString());
			Log.i(TAG, "---------------------------FIRST img = " + rgbaImage.dump());
			Log.i(TAG, "---------------------------SECOND img = " + rgbaImageLast.toString());
			Log.i(TAG, "---------------------------SECOND img = " + rgbaImageLast.dump());
			Core.absdiff(rgbaImage,rgbaImageLast, rgbaImage); // alpha channel becomes 0 through subtraction
			// 
			Log.i(TAG, "---------------------------DIFF img = " + rgbaImage.toString());
			Log.i(TAG, "---------------------------DIFF img = " + rgbaImage.dump());
			//Core.subtract(rgbaImage,rgbaImageLast, rgbaImage);
			Core.add(rgbaImageLast, new Scalar(32, 32, 32, 255), rgbaImageLast);
	*/
			//Core.add(result, new Scalar(0, 0, 0, 255), result);
			//Core.absdiff(rgbaImage,rgbaImageLast, result);
			//Core.add(result, new Scalar(0, 0, 0, 255), result);
			//Core.log(result, result);
			//Core.subtract(rgbaImageLast,rgbaImage, rgbaImageLast);
			//Core.add(rgbaImageLast, new Scalar(32), rgbaImageLast);
			//Core.add(rgbaImageLast, rgbaImage, rgbaImageLast);
			//Core.add(rgbaImageLast, new Scalar(5), rgbaImageLast);
			
			/*Scalar sum = Core.sumElems(rgbaImage);
			Log.i(TAG, "---------------------------sum of diff img = " + sum.toString() );
			sum = Core.sumElems(rgbaImageLast);
			Log.i(TAG, "---------------------------sum of diff img = " + sum.toString() );
			*/
			//Core.multiply(rgbaImageLast, new Scalar(50), rgbaImageLast);
			//Imgproc.Canny(result, result, 80, 100);
			
			//Imgproc.Sobel(rgbaImage,rgbaImage,rgbaImage.depth(),3,3);
			//Imgproc.Sobel(rgbaImageLast,rgbaImageLast,rgbaImageLast.depth(),3,3);
			//Core.subtract(rgbaImageLast,rgbaImage, rgbaImage);
			//Core.add(rgbaImage, new Scalar(0, 0, 0, 255), rgbaImage);
		}
		
	
		if(doEdge)
		{
			//Canny edge detection
			//Imgproc.Canny(rgbaImage, rgbaImage, 80, 100);
			//Imgproc.Sobel(rgbaImage,rgbaImage,rgbaImage.depth(),3,3);
		
		}
		
		if(doHough)
		{
			// Hough Circle detection
			Mat circles = new Mat();
			double dp = 2.0; // works when image is half scale
			//double dp = 1.0/0.8;
			double minDist = 50.0;
			int method = Imgproc.CV_HOUGH_GRADIENT;

			// Apply Gauss blur to reduce false circles			
			//Imgproc.GaussianBlur(rgbaImage,rgbaImage, new Size(5,5), 1);
			Imgproc.HoughCircles(rgbaImage, circles, method, dp, minDist, 80, 90, 50, 100 );
			////Imgproc.HoughCircles(greyImage, circles, method, dp, minDist, 80, 90, 50, 200 );
			//Imgproc.HoughCircles(rgbaImage, circles, method, dp, minDist);
			Imgproc.Canny(rgbaImage, rgbaImage, 80, 90);
	
			// Draw circles
			Imgproc.cvtColor(rgbaImage, rgbaImage, Imgproc.COLOR_GRAY2RGB);
			int numCircles = (int) (circles.total()); // 1xN matrix
			//Log.i(TAG, "---------------------------number of CIRCLES = " + circles.total() );
			//Log.i(TAG, "---------------------- img w/h = " + rgbaImage.cols() + " " + rgbaImage.rows() );
			
			Core.putText(rgbaImage, new String(Integer.toString(numCircles) + " circles"), new Point(200, 200), 
					3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 2);
	
			// draw circles	
			double[] pt = new double[3];

			for(int i=0; i<numCircles; i++)
			{
				if(numCircles > 0) 
				{	
					
					// (row, col) - 1 x N circles (rows x cols)
					pt = circles.get(0, i); // 0-index!!!!
					int r = (int)(pt[2]);
						
					//Log.i(TAG, "---------------------- (x,y) = (" + pt[0] + "," + pt[1] + ")" );
					Core.circle(rgbaImage, new Point(pt), r, new Scalar(0, 255, 0, 255));
						
					//Point ctr = new Point(Math.round(pt[0]),Math.round(pt[0])); // we have the (x,y) points of our circles
					//Log.i(TAG, "---------------------- (x,y) = (" + ctr.x + "," + ctr.y + ")" );
					//int r = (int)(Math.round(pt[3]));
					//Core.circle(rgbaImage, ctr, r, new Scalar(0, 255, 0, 255));
				}
			}	
		}
			
	}
	
	private Mat Scalar(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public void process(Mat rgbaImage)
	{
    	Mat pyrDownMat = new Mat();

    	// Downscale image by factor of 4 (applying Gaussian blur, then downsample)
    	Imgproc.pyrDown(rgbaImage, pyrDownMat);
    	Imgproc.pyrDown(pyrDownMat, pyrDownMat);
  
    	// Convert to HSV
      	Mat hsvMat = new Mat();
    	Imgproc.cvtColor(pyrDownMat, hsvMat, Imgproc.COLOR_RGB2HSV_FULL);
    	// Create binary mask by thresholding the upper and lower bound 
    	// of touched area (cool!)
    	Mat Mask = new Mat();
    	Core.inRange(hsvMat, mLowerBound, mUpperBound, Mask);
    	// Apply dilation to mask
    	Mat dilatedMask = new Mat();
    	Imgproc.dilate(Mask, dilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        
        // Detect contours
        Imgproc.findContours(dilatedMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
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
        	if (Imgproc.contourArea(contour) > mMinContourArea*maxArea)
        	{
        		Core.multiply(contour, new Scalar(4,4), contour);
        		mContours.add(contour);
        	}
        }
	}

	public List<MatOfPoint> getContours()
	{
		return mContours;
	}
	
	
	// Mean Greyscale value for blob detection
	private Scalar mGreyscale = new Scalar(0);
	// Lower and Upper bounds for range checking in HSV color space
	private Scalar mLowerBound = new Scalar(0);
	private Scalar mUpperBound = new Scalar(0);
	// Minimum contour area in percent for contours filtering
	private static double mMinContourArea = 0.1;
	// Color radius for range checking in HSV color space
	private Scalar mColorRadius = new Scalar(15,0,0,0);// tight range because we are in greyscale
	//private Scalar mColorRadius = new Scalar(25,50,50,0);
	private Mat mSpectrum = new Mat();
	private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();;
	
	//Logcat tag
	private static final String TAG = "ColorBlobDetector::process()";

}
