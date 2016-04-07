package com.chervital.coordinates;

//import android.support.v7.app.ActionBarActivity;

import Marshals.MarshalDate;
import Marshals.MarshalDouble;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.URL;
import java.util.Date;

import javax.xml.namespace.QName;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.ksoap2.serialization.Marshal;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.IBinder;

public class MainActivity extends Activity {

  TextView tvEnabledGPS;
  TextView tvStatusGPS;
  TextView tvLocationGPS;
  TextView tvEnabledNet;
  TextView tvStatusNet;
  TextView tvLocationNet;

  private LocationManager locationManager;
  StringBuilder sbGPS = new StringBuilder();
  StringBuilder sbNet = new StringBuilder();
  int UpdateInterval = 0;
  int UpdateDistance = 1;
  
  private static final String SOAP_ACTION = "http://87.249.208.99:7777/ws/hello/getCooordinate";
  private static final String OPERATION_NAME = "getCooordinate";
  private static final String WSDL_TARGET_NAMESPACE = "http://Server/";
  private static final String SOAP_ADDRESS = "http://87.249.208.99:7777/ws/hello";
	final String LOG_TAG = "CoordLogs";
	
  public double lon, lat;
  boolean bound = false;
  ServiceConnection sConn;
  Intent intent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
    tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
    tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
    tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
    tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
    tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);

    //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    
    intent = new Intent(this,CoordService.class);
    
    sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
          Log.d(LOG_TAG, "MainActivity onServiceConnected");
          bound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
          Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
          bound = false;
        }
    };
        
    startService(intent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    /*
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        1000 * UpdateInterval, UpdateDistance, locationListener);
    locationManager.requestLocationUpdates(
        LocationManager.NETWORK_PROVIDER, 1000 * UpdateInterval, UpdateDistance,
        locationListener);
    checkEnabled();
    */
  }

  @Override
  protected void onPause() {
    super.onPause();
    //locationManager.removeUpdates(locationListener);
  }

  private LocationListener locationListener = new LocationListener() {

    @Override
    public void onLocationChanged(Location location) {
      showLocation(location);
      SendLocation(location);
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
    	 Object response = envelope.getResponse();
    	 } catch (Exception exception) {
    		 System.out.println(exception.toString());
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
        tvStatusGPS.setText("Status: " + String.valueOf(status));
      } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
        tvStatusNet.setText("Status: " + String.valueOf(status));
      }
    }
  };

  private void showLocation(Location location) {
    if (location == null)
      return;
    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
      tvLocationGPS.setText(formatLocation(location));
    } else if (location.getProvider().equals(
        LocationManager.NETWORK_PROVIDER)) {
      tvLocationNet.setText(formatLocation(location));
    }
  }

  private String formatLocation(Location location) {
    if (location == null)
      return "";
    return String.format(
        "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
        location.getLatitude(), location.getLongitude(), new Date(
            location.getTime()));
  }

  private void checkEnabled() {
    tvEnabledGPS.setText("Enabled: "
        + locationManager
            .isProviderEnabled(LocationManager.GPS_PROVIDER));
    tvEnabledNet.setText("Enabled: "
        + locationManager
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
  }

  public void onClickLocationSettings(View view) {
	  stopService(intent);
    //startActivity(new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
  };

}
