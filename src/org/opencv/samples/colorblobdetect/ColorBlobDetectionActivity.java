package org.opencv.samples.colorblobdetect;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.example.colorblobdetect.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
 
// For iSENSE upload, etc.
import edu.uml.cs.isense.comm.RestAPI;

public class ColorBlobDetectionActivity extends Activity {
	
	Boolean useDevSite = false;

	private static final String TAG = "Example/ColorBlobDetection";
	private ColorBlobDetectionView mView;
	public static Context mContext;

	// iSENSE uploader
	RestAPI rapi;
	
	// iSENSE login

	private static String userName = "videoAnalytics";
	private static String password = "videoAnalytics";
	
	// create session name based upon first name and last initial user enters
    static String firstName = "";
    static String lastInitial = "";
    private final int ENTERNAME_REQUEST = -4;
    Boolean sessionNameEntered = false;
	
	//private static String experimentNumber = "586"; // production
	private static String experimentNumber = "598"; // dev
	private static String baseSessionUrl   = "http://isense.cs.uml.edu/newvis.php?sessions=";
	private static String baseSessionUrlDev   = "http://isensedev.cs.uml.edu/newvis.php?sessions=";
	private static String sessionUrl = "";
	
	private String dateString;
	
	// upload progress dialogue
	ProgressDialog dia;
	// JSON array for uploading pendulum position data,
	// accessed from ColorBlobDetectionView
	public static JSONArray dataSet;
	
	   private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
	    	@Override
	    	public void onManagerConnected(int status) {
	    		switch (status) {
					case LoaderCallbackInterface.SUCCESS:
					{
						Log.i(TAG, "OpenCV loaded successfully");
						// Create and set View
						///mView = new ColorBlobDetectionView(mAppContext);
						///setContentView(mView);
						mView = new ColorBlobDetectionView(mAppContext);
						setContentView(mView);
						
						// Check native OpenCV camera
						if( !mView.openCamera() ) {
							AlertDialog.Builder ad = new AlertDialog.Builder(mAppContext);
							ad.setCancelable(false); // This blocks the 'BACK' button
							ad.setMessage("Fatal error: can't open camera!");
							ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							    }
							});
							ad.show();
						}
					} break;
					default:
					{
						super.onManagerConnected(status);
					} break;
				}
	    	}
		};
	
	public ColorBlobDetectionActivity()
	{
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

    @Override
	protected void onPause() {
        Log.i(TAG, "onPause");
		super.onPause();
	//	if (null != mView)
		//	mView.releaseCamera();
	}

	@Override
	protected void onResume() {
        Log.i(TAG, "onResume");
		super.onResume();
		if( (null != mView) && !mView.openCamera() ) {
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setCancelable(false); // This blocks the 'BACK' button
			ad.setMessage("Fatal error: can't open camera!");
			ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			    }
			});
			ad.show();
		}
		
	}

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
	   
	   switch(item.getItemId())
	   {
	   
	   // stop experiment and data collection
	   case R.id.menu_upload:
	
		   // Add iSENSE upload code here!
			
		   // login to iSENSE if not already
		   if(rapi.isConnectedToInternet())
		   {
			   Log.i(TAG, "Connected to the 'net.");
			   Boolean status;
			   // attempt to upload data if logged in
			   if(!rapi.isLoggedIn())
			   {
				   status = rapi.login(userName, password);
				   
				   if(!status)
				   {
			   		   Toast.makeText(this, "Unable to log into iSENSE. Invalid user id? Try again.",
		    					Toast.LENGTH_LONG).show();  
			   		   return false;
				   }
			   }
			   
			   if(rapi.isLoggedIn())
			   {
   
				   // upload data backgrounded in a thread
				   // onActivity
				   //if(sessionNameEntered)
				   if(firstName.length() > 0 || lastInitial.length() > 0)
				   {
					   if(mView.dataCollectionEnabled())
					   {
						   new uploadTask().execute();
					   }
					   else
					   {
						   Toast.makeText(ColorBlobDetectionActivity.this, "You must start data collection before uploading to iSENSE!", Toast.LENGTH_LONG).show();
					   }
				   }
				   else
				   {
			   		   Toast.makeText(this, "You must first start data collection to create session name.",
		    					Toast.LENGTH_LONG).show();
			   		   
			   		   return false;
				   }
					  
			   }
	
		   }
	   	   else
	       {
	   		   Toast.makeText(this, "You are not connected to the Intertubes. Check connectivity and try again.",
	    					Toast.LENGTH_LONG).show();
	   		   return false;
	       }
		   
		   return true;
		   
	   // start experiment and data collection
	   case R.id.menu_start:   

		   // create session name with user first name and last initial
		   // if we are logged in
		   if(firstName.length() == 0 || lastInitial.length() == 0)
		   {
	 		    //	Boolean dontPromptMeTwice = true;
	 			startActivityForResult(
	     	   			new Intent(mContext, LoginActivity.class),
	     	   			ENTERNAME_REQUEST);
		   }
		   
		   
		   AlertDialog.Builder startBuilder = new AlertDialog.Builder(this); // 'this' is an Activity - can add an ID to this like CRP
		   // chain together various setter methods to set the dialog characteristics
		   startBuilder.setMessage("Pull pendulum to edge of screen and hit 'OK' to start collecting data.")
	          .setTitle("Instructions:")
	          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              // @Override
               public void onClick(DialogInterface dialog, int id) {
            	   // grab position of target and pass it along
            	   // If this were Cancel for setNegativeButton() , just do nothin'!
        		   
        		   // clear existing data in JSON array (for upload to iSENSE)
        		   dataSet = new JSONArray();
        		  
        		   // start data collection
        		   mView.startDataCollection(dataSet);
               }
              
               
           });
		   
		   // get the AlertDialog from create()
		   AlertDialog startDialog = startBuilder.create();
		   startDialog.show(); // make me appear!
		   		   
		   return true;
		   
	   case R.id.menu_exit:
		   // Exit app neatly
		   this.finish();
		   return true;
		   
	   case R.id.menu_instructions:   

		   String strInstruct = "Center at-rest pendulum in center of image. Select 'Start data collection button' to start. Pull pendulum back to left or right edge of image and release when selecting 'OK'. Select 'Stop and upload to iSENSE' to stop. ";
	
		   AlertDialog.Builder builder = new AlertDialog.Builder(this); // 'this' is an Activity - can add an ID to this like CRP
		   // chain together various setter methods to set the dialog characteristics
		   builder.setMessage(strInstruct)
	          .setTitle("Instructions:")
	          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              // @Override
               public void onClick(DialogInterface dialog, int id) {
            	   // grab position of target and pass it along
            	   // If this were Cancel for setNegativeButton() , just do nothin'!
            	   
               }
              
               
           });
		   
		   // get the AlertDialog from create()
		   AlertDialog dialog = builder.create();
		   dialog.show(); // make me appear!
		   
		   return true;
	   }
	   return true;
   }
   
   
/*
    @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	
	if (requestCode == ENTERNAME_REQUEST) {
		if (resultCode == LoginActivity.NAME_SUCCESSFUL) {
			//dance
			sessionNameEntered = true;
		    
		}
	}
}
*/

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
       
        // add LinearLayout
        ///setContentView(R.layout.main);
        
        ///mView = (ColorBlobDetectionView) findViewById(R.id.detectView);
        
        // set context (for starting new Intents,etc)
        mContext = this;
       
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mOpenCVCallBack))
        {
        	Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        
        // iSENSE network connectivity stuff
        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
        rapi.useDev(useDevSite);
        
    
    }
   
   
 
	private Runnable uploader = new Runnable() {
			
		//@Override
		public void run() {

			// stop data collection for upload to iSENSE
			dataSet = new JSONArray();
			dataSet = mView.stopDataCollection();
							
			// Create location-less session (for now)
			int sessionId = -1;
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");
		    Date dt = new Date();
		    dateString = sdf.format(dt);
		    
			String nameOfSession = firstName + " " +  lastInitial + ". - " + dateString;
			//String nameOfSession = "underpantsGnomes";
			 
			sessionId = rapi.createSession(experimentNumber, 
											nameOfSession + " (location not found)", 
											"Automated Submission Through Android App", 
											"", "", "");
			if(useDevSite)
			{
				sessionUrl = baseSessionUrlDev + sessionId; 
				Log.i(TAG, sessionUrl);
			}
			else
				sessionUrl = baseSessionUrl + sessionId;
				
			Log.i(TAG, "Putting session data...");
			rapi.putSessionData(sessionId, experimentNumber, dataSet);
	
		}
		
	};
	   
    
    // Task for uploading data to iSENSE
	public class uploadTask extends AsyncTask <Void, Integer, Void> {

	    
	    @Override protected void onPreExecute() {
	     	
	        dia = new ProgressDialog(ColorBlobDetectionActivity.this);
	        dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dia.setMessage("Please wait while your data is uploaded to iSENSE...");
	        dia.setCancelable(false);
	        dia.show();       
	      
	    }

	    @Override protected Void doInBackground(Void... voids) {

	        uploader.run();
	        publishProgress(100);
	        return null;
	        
	    }

	    @Override  protected void onPostExecute(Void voids) {
	        
	    	dia.setMessage("Done");
	        dia.cancel();
	        
	        Toast.makeText(ColorBlobDetectionActivity.this, "Data upload successful!", Toast.LENGTH_SHORT).show();

	    }
	}	
	
	
}