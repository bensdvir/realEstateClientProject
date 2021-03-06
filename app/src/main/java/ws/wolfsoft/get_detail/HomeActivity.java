package ws.wolfsoft.get_detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Communication.Communication;
import DataObjects.Apartment;
import DataObjects.User;

public class HomeActivity extends AppCompatActivity  implements OnMapReadyCallback {
    private static final String TAG = "";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public HashMap<String,Bundle> apartments = new HashMap<>();
    static public HashMap<String,byte[]> apartmentsImages = new HashMap<>();

    public HashMap<LatLng,String> apartmentsAddresses = new HashMap<>();
    public static HashMap<String,Float> aparttmentsRatings = new HashMap<>();
    public static HashMap<String,Apartment> homeToSearch = new HashMap<>();
    public static HashMap<String,Apartment> searchToHome = new HashMap<>();


    public static HashMap<String,Float> usersRatings = new HashMap<>();
    boolean preesed = false;



    private GoogleMap mMap;
    static byte[] tmpImage= null;
    private int n;
    private PopupWindow mPopupWindow;
    private String m_Text = "";
    private List<Apartment> ans = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location myLoc;
    LocationManager locationManager;
    LocationManager mLocationManager;
    //Location myLocation = getLastKnownLocation();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
        final Button locButton = (Button) findViewById(R.id.buttonLoc);
        final Button resButton = (Button) findViewById(R.id.buttonRestart);
        final Button chatButton = (Button) findViewById(R.id.buttonChat);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        if(LoginActivity.isAno){
                            HomeActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(HomeActivity.this, "You can't access chat", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });
                            return null;
                        }



                        String url = "https://finalproject-72668.firebaseio.com/users.json";
                        String user = LoginActivity.username;
                        UserDetails.username = user;
                        UserDetails.password = "123456";
                        String pass = "123456";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                    try {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(s);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(!obj.has(user)){
                            Toast.makeText(HomeActivity.this, "user not found", Toast.LENGTH_LONG).show();
                        }
                        else if(obj.getJSONObject(user).getString("password").equals(pass)){
                            UserDetails.username = user;
                            UserDetails.password = pass;
                            startActivity(new Intent(HomeActivity.this, Users.class));    //for all users
                        }
                        else {
                            Toast.makeText(HomeActivity.this, "incorrect password", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(HomeActivity.this);
        rQueue.add(request);
                        return null;
                    }
                }.execute();
            }
        });


        locButton.setBackgroundResource(R.drawable.loc_black);
        resButton.setBackgroundResource(R.drawable.res_button);



        final Button fastSearch = (Button) findViewById(R.id.buttonCatSearch);
        fastSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Intent intent = new Intent(HomeActivity.this,CatSearchActivity.class);
                        Bundle bundle = new Bundle();
                        int i = 0;
                        for (Apartment ap : ans) {
                            homeToSearch.put("ap"+i,ap);
                            //intent.putExtra("ap" + i, ap);
                            i += 1;
                        }
                        bundle.putInt("apartmentsNum", homeToSearch.size());
                        if(getIntent().getExtras().containsKey("idFacebook")) {
                            bundle.putString("idFacebook", getIntent().getExtras().get("idFacebook").toString());
                        }
                        if(getIntent().getExtras().containsKey("idFacebook")) {
                            bundle.putString("sessionId", getIntent().getExtras().get("idFacebook").toString());
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);




                        /////////////



                        /////////////////////////////////////////////////////////////////////////

                        return null;
                    }
                }.execute();
            }
        });



        resButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        getIntent().removeExtra("isInSearch");
                        finish();
                        startActivity(getIntent());




                        /////////////



                        /////////////////////////////////////////////////////////////////////////

                        return null;
                    }
                }.execute();
            }
            });


        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //locButton.setClickable(false);
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                else
                {
                    if(canGetLocation()) {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Location lastKnownLocation = getLastKnownLocation();
                                    LatLng newLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17.4f));
                                }
                            catch (Exception e){

                                HomeActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(HomeActivity.this, "Can't get location", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            }
                        }, 1000);


                    }
                    else{
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }

                    //displayLocationSettingsRequest( HomeActivity.this);
                    // Write you code here if permission already given.
                }
            }
        });

        HomeActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                android.support.v7.widget.Toolbar tb = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
            LinearLayout profileLayout = (LinearLayout) tb.findViewById(R.id.profileLayout);
            LinearLayout logoutLayout = (LinearLayout) tb.findViewById(R.id.logoutLayout);
            LinearLayout searchLayout = (LinearLayout) tb.findViewById(R.id.searchLayout);

        searchLayout.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                Bundle bundle = new Bundle();
                int i = 0;
                for (Apartment ap : ans) {
                    homeToSearch.put("ap"+i,ap);
                    //intent.putExtra("ap" + i, ap);
                    i += 1;
                }
                bundle.putInt("apartmentsNum", homeToSearch.size());
                if(getIntent().getExtras().containsKey("idFacebook")) {
                    bundle.putString("idFacebook", getIntent().getExtras().get("idFacebook").toString());
                }
                if(getIntent().getExtras().containsKey("idFacebook")) {
                    bundle.putString("sessionId", getIntent().getExtras().get("idFacebook").toString());
                }
                intent.putExtras(bundle);
                startActivity(intent);
                }
            });


        profileLayout.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                   // ConversationActivity.show(HomeActivity.this);


                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                Bundle facebookData = getIntent().getExtras();
                facebookData.putString("isProfile","yes");

                if(LoginActivity.isAno){
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            startActivity(intent);
                            return null;
                        }
                    }.execute();
                }

                if(LoginActivity.sessionId!=null) {
                    // b.putString("idFacebook", LoginActivity.sessionId);
                    try {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                HashMap<String, String> header = new HashMap<String, String>();
                                header.put("token", facebookData.getString("sessionId"));
                                User response = Communication.makeGetRequest(Communication.ip + "/user/getByToken", header, User.class);
                                //Bundle b = userToBundle(response);
                                User user = response;
                                Bundle b = new Bundle();
                                b.putString("email", user.getEmail());
                                b.putString("firstName", user.getFirstName());
                                b.putString("gender", user.getGender());
                                b.putString("rank", user.getAvgRankRanker().toString());
                                b.putString("image", user.getImage());
                                b.putString("lastName", user.getLastName());
                                b.putString("token", user.getToken());
                                b.putString("isProfile", "yes");
                                b.putString("sessionId", facebookData.getString("sessionId"));
                                intent.putExtras(b);
                                startActivity(intent);
                                return null;
                            }
                        }.execute();
                    } catch (Exception e) {
                        HomeActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(HomeActivity.this, "Some bug", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            startActivity(intent);
                            return null;
                        }
                    }.execute();

                }

                }
            });

        logoutLayout.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick (View view){
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                if (getIntent().getExtras().containsKey("sessionId")) {
                    LoginManager.getInstance().logOut();
                }
                    try {
                        startActivity(intent);}
                    catch (Exception e){}            }
            });
        }});
    }

    public void onMapReady(GoogleMap googleMap) {
        Bundle b = getIntent().getExtras();
        mMap = googleMap;
        LatLng home = new LatLng(32,35);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //LatLng newLatLng = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        //mMap.addMarker(new MarkerOptions().position(home).title("Marker in Home"));

        HomeActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                List<Apartment> result = getAllApartmentsFromServer();
                while (ans == null) {
                }

                if (!getIntent().getExtras().containsKey("isInSearch")) {
                    setApartmentsOnMap(ans);
                } else {
                    int numAp = getIntent().getExtras().getInt("apartmentsNum");
                    List<Apartment> allResults = new ArrayList<Apartment>();
                    for (HashMap.Entry<String, Apartment> entry : HomeActivity.searchToHome.entrySet())
                    {
                        allResults.add(entry.getValue()) ;
                    }
                    searchToHome = new HashMap<>();
                    /*for (int i = 0; i < numAp; i++) {
                        Apartment tmp = (Apartment) getIntent().getExtras().get("ap" + i);
                        allResults.add(tmp);
                    }
                   */

                    setApartmentsOnMap(allResults);
                }
            }
        });

        mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            //onInfoWindowClick();

            @Override
            public void onInfoWindowClick(Marker marker) {
                onMarkerChosen(marker);
        }});


    }


    public void setApartmentsOnMap(List<Apartment> ans) {
        if (ans==null){
        }
        for (Apartment ap: ans){
            aparttmentsRatings.put(ap.getAddress(),null);
        }
        Geocoder coder = new Geocoder(this);
        for(Apartment ap: ans){
            String strAddress = ap.getAddress();
            try {
                List<Address> address = coder.getFromLocationName(strAddress,5);
                if(address.isEmpty()){
                    continue;
                }
                Address location = address.get(0);
                LatLng lg = new LatLng(location.getLatitude(), location.getLongitude());
                apartmentsAddresses.put(lg,strAddress);
                mMap.addMarker(new MarkerOptions().position(lg).title(strAddress));
                apartments.put(strAddress,apartmentToBundle(ap));
                apartmentsImages.put(strAddress, ap.getImage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Apartment> getAllApartmentsFromServer(){
        new AsyncTask<Void, Void, List<Apartment>>() {
            @Override
            protected List<Apartment> doInBackground(Void... params) {
                HashMap<String,String> header = new HashMap<String,String>();
                if (!LoginActivity.isAno) {
                    header.put("token", LoginActivity.sessionId);
                }
                else {
                    header.put("token", "null");
                }
                //header.put("token",getIntent().getExtras().get("idFacebook").toString());
                List<Apartment> response = Communication.makeGetRequestGetList(Communication.ip+"/apartment/getAll", header, Apartment.class);
                ans = response;//
                return response;
            }
        }.execute();
        return null;
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public void onMarkerChosen(Marker marker) {
        //DialogFragment newFragment = new DialogFragment();
        //newFragment.setStyle(4,0);
        //newFragment.show(getSupportFragmentManager(), "missiles");



        HomeActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Intent intent = new Intent(getBaseContext(), ApartmentActivity.class);
                LatLng lg = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                String ad = apartmentsAddresses.get(lg);
                Bundle b = apartments.get(ad);
                if(getIntent().getExtras().containsKey("sessionId"))
                    b.putString("sessionId",getIntent().getExtras().get("sessionId").toString());
                intent.putExtras(b);
                try {
                    startActivity(intent);}
                catch (Exception e){}
            }
        });

    }


    private Bundle apartmentToBundle(Apartment ap){
        Bundle b = new Bundle();
        b.putString("address", ap.getAddress());
        b.putInt("price", ap.getPrice().intValue());
        b.putInt("floor", ap.getFloor().intValue());
        b.putBoolean("elevator", ap.getElevator());
        b.putBoolean("wareHouse", ap.getWareHouse());
        b.putBoolean("parking", ap.getParking());
        b.putInt("constructionYear", ap.getConstructionYear().intValue());
        b.putString("description", ap.getDescription());
        b.putInt("numToilets", ap.getNumToilet().intValue());
        b.putInt("numRooms",ap.getNumRooms().intValue());
        b.putDouble("size",ap.getSize());
        b.putDouble("averageRank", ap.getAverageRank());
        //b.putString("image",ap.getImage());
        b.putString("landLordID",ap.getLandLordID());
        b.putBoolean("isRent",ap.getIsRent());

        return b;
    }


    private Bundle userToBundle(User user){
        Bundle b = new Bundle();
        b.putString("email", user.getEmail());
        b.putString("fistName", user.getFirstName());
        b.putString("gender", user.getGender());
        b.putString("image", user.getImage());
        b.putString("lastName", user.getLastName());
        b.putString("token", user.getToken());
        b.putDouble("avglandrank", user.getAvgRankLandLoard());
        b.putDouble("avgRankrank", user.getAvgRankRanker());
        return b;
    }


}



