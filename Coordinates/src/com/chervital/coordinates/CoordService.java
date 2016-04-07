package com.chervital.coordinates;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import Marshals.MarshalDate;
import Marshals.MarshalDouble;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;



public class CoordService extends Service {
	
	
	final String LOG_TAG = "CoordLogs";
	
	  private LocationManager locationManager;
	  StringBuilder sbGPS = new StringBuilder();
	  StringBuilder sbNet = new StringBuilder();
	  int UpdateInterval = 0;
	  int UpdateDistance = 1;
	  
	  private static final String SOAP_ACTION = "http://87.249.208.99:7777/ws/hello/getCooordinate";
	  private static final String OPERATION_NAME = "getCooordinate";
	  private static final String WSDL_TARGET_NAMESPACE = "http://Server/";
	  private static final String SOAP_ADDRESS = "http://87.249.208.99:7777/ws/hello";
	  public double lon, lat;
	  NotificationManager nm;
	  
	  private static final int NOTIFY_ID = 101;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	


	  public void onCreate() {
	    super.onCreate();
	    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	    nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    Log.d(LOG_TAG, "onCreate");
	  }
	  
	  public int onStartCommand(Intent intent, int flags, int startId) {
		    Log.d(LOG_TAG, "MyService onStartCommand");
		    readFlags(flags);
		    MyRun mr = new MyRun(startId);
		    new Thread(mr).start();

  		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		            1000 * UpdateInterval, UpdateDistance, locationListener);
		    locationManager.requestLocationUpdates(
		            LocationManager.NETWORK_PROVIDER, 1000 * UpdateInterval, UpdateDistance,
		            locationListener);

		        //checkEnabled();
		        
		    return  START_STICKY;
	  }

	  
	  public void onDestroy() {
	    super.onDestroy();
	    locationManager.removeUpdates(locationListener);
	    Log.d(LOG_TAG, "onDestroy");
	  }
	  
	  void readFlags(int flags) {
		    if ((flags&START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY) 
		      Log.d(LOG_TAG, "START_FLAG_REDELIVERY");  
		    if ((flags&START_FLAG_RETRY) == START_FLAG_RETRY) 
		      Log.d(LOG_TAG, "START_FLAG_RETRY");
		  }
	  
	  private LocationListener locationListener = new LocationListener() {

		    @Override
		    public void onLocationChanged(Location location) {
		      //showLocation(location);
		      Log.d(LOG_TAG, "SendLocation");
		      SendLocation(location);
		    }

		    void SendNotif(String NotifText) {

		    	
		    	Context context = getApplicationContext();

		        
		        Intent notificationIntent = new Intent(context, MainActivity.class);
		        PendingIntent contentIntent = PendingIntent.getActivity(context,
		                0, notificationIntent,
		                PendingIntent.FLAG_CANCEL_CURRENT);

		        Resources res = context.getResources();
		        Notification.Builder builder = new Notification.Builder(context);

		        builder.setContentIntent(contentIntent)                
		        .setSmallIcon(R.drawable.abc_search_dropdown_light)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.abc_spinner_ab_focused_holo_light))
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("ОТправка координат")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("ОТправлено")
                //.setContentText(res.getString(R.string.notifytext))
                .setContentText(NotifText); // Текст уведомления
		        
		        Notification notification = builder.build();
		        
	        
		        nm.notify(NOTIFY_ID, notification);
		        

		      }
		    
		    private void SendLocation(Location location) {
				// TODO Auto-generated method stub
		    	

		    	
		    	 lon = location.getLongitude();
		    	 lat = location.getLatitude();
		    	
				Runnable r1 = new Runnable() {
			        public void run() 
			        {
			        	 try {
			        		 

		    	 SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);

		         PropertyInfo pi = new PropertyInfo();
		         pi.setName("arg0");
		         pi.setValue(lat);
		         request.addProperty(pi);
		  
		         PropertyInfo pi2 = new PropertyInfo();
		         pi2.setName("arg1");
		         pi2.setValue(lon);
		         request.addProperty(pi2);
		 
		    
		         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope( SoapEnvelope.VER11);
		    	 MarshalDouble md = new MarshalDouble();
		    	 md.register(envelope);
		    	 
		    	 MarshalDate mdt = new MarshalDate();
		    	 mdt.register(envelope);
		    	 
		    	 envelope.dotNet = false;
		    	 envelope.setOutputSoapObject(request);

		    	 
		    	 HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
		 
		    	 try { httpTransport.call(SOAP_ACTION, envelope);
		    	 SoapPrimitive resultsString = (SoapPrimitive)envelope.getResponse();
		    	 SendNotif(resultsString.toString());
		    	 //Object response = envelope.getResponse();
		    	 } 
		    	 catch ( InterruptedIOException e ) {
		    		 	SendNotif("Ошибка связи с сервером");
		    		   
		    		    } catch ( IOException e ) {
		    		    	SendNotif("Неопознанная ошибка");
		    		    }
		    	 catch (Exception exception) {
		    		 SendNotif("Ошибка");
		    	 };
			        	 }
			        	 finally
			        	 {
			        		 
			        	 };
			        };
				};
				Thread thread = new Thread(r1);
				thread.start();

			}

			@Override
		    public void onProviderDisabled(String provider) {
		      checkEnabled();
		    }

		    @Override
		    public void onProviderEnabled(String provider) {
		      checkEnabled();
		      showLocation(locationManager.getLastKnownLocation(provider));
		    }

		    @Override
		    public void onStatusChanged(String provider, int status, Bundle extras) {
		      if (provider.equals(LocationManager.GPS_PROVIDER)) {
		        //tvStatusGPS.setText("Status: " + String.valueOf(status));
		      } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
		        //tvStatusNet.setText("Status: " + String.valueOf(status));
		      }
		    }
		  };

	  private void checkEnabled() {
			  
		  }
		  
	  private void showLocation(Location location) {
		    if (location == null)
		      return;
		    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
		      //tvLocationGPS.setText(formatLocation(location));
		    } else if (location.getProvider().equals(
		        LocationManager.NETWORK_PROVIDER)) {
		      //tvLocationNet.setText(formatLocation(location));
		    }
		  }
	  
	  class MyRun implements Runnable {

		    int startId;

		    public MyRun(int startId) {
		      this.startId = startId;
		      
		      Log.d(LOG_TAG, "MyRun#" + startId + " create");
		    }

		    public void run() {
		      Log.d(LOG_TAG, "MyRun#" + startId + " start");
		      try {
/*
				    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,

				            1000 * UpdateInterval, UpdateDistance, locationListener);
				    locationManager.requestLocationUpdates(
				            LocationManager.NETWORK_PROVIDER, 1000 * UpdateInterval, UpdateDistance,
				            locationListener);		
				             */    	  
		        TimeUnit.SECONDS.sleep(20);
//			    locationManager.removeUpdates(locationListener);
		      } catch (InterruptedException e) {
		        Log.d(LOG_TAG, e.toString());
		      }
		      //stop();
		    }

		    void stop() {
		      Log.d(LOG_TAG, "MyRun#" + startId + " end, stopSelfResult("
		          + startId + ") = " + stopSelfResult(startId));
		    }
		  }

}

