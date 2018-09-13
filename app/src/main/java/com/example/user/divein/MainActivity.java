package com.example.user.divein;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseUser mCurrentUser;

    private GoogleMap mMap;

    private EditText mSearchET;

    private DatabaseReference mDiveSitesDataBase;
    private DatabaseReference mWishlistDatabase;

    private static final float DEFAULT_ZOOM = 12f;

    private static final int RC_PERM = 124;

    private float mCurrentLocationAvg = 0f;

    private RatingBar mRatingBar;

    private CheckBox mDiveSitesCB;
    private CheckBox mDiveClubsCB;

    private ArrayList<Marker> mSitesArray;
    private ArrayList<Marker> mClubsArray;

    private boolean mFromRegisterPage = false;

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else {
            loadNavHeaderDetails();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFromRegisterPage = getIntent().getBooleanExtra("from register", false);
        if(mFromRegisterPage){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Welcome!!!").setMessage("Congratulations for registering DiveIn App! " +
                    "You are now part of a great scuba diving community :)")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }


        requestPermissions();

        mSitesArray = new ArrayList<>();
        mClubsArray = new ArrayList<>();

        if(mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDiveSitesDataBase = FirebaseDatabase.getInstance().getReference().child("Coordinates");
        mWishlistDatabase = FirebaseDatabase.getInstance().getReference().child("Wishlists");

        mSearchET = (EditText) findViewById(R.id.search_edit_text);
        mSearchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocate();
                }

                return false;
            }
        });

        mDiveClubsCB = (CheckBox) findViewById(R.id.dive_club_checkbox);
        mDiveSitesCB = (CheckBox) findViewById(R.id.dive_site_checkbox);

        mDiveClubsCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    for(Marker marker : mClubsArray) {
                        marker.setVisible(true);
                    }
                }else{
                    for(Marker marker : mClubsArray) {
                        marker.setVisible(false);
                    }
                }
            }
        });

        mDiveSitesCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    for(Marker marker : mSitesArray) {
                        marker.setVisible(true);
                    }
                }else{
                    for(Marker marker : mSitesArray) {
                        marker.setVisible(false);
                    }
                }
            }
        });
    }

    private void loadNavHeaderDetails() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);

        final CircleImageView userImage = (CircleImageView) hView.findViewById(R.id.nav_header_imageview);
        TextView usernameTV = (TextView) hView.findViewById(R.id.nav_header_username);

        if(mCurrentUser.getPhotoUrl() != null)
        {
            Picasso.get().load(mCurrentUser.getPhotoUrl())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.progress_animation)
                    .into(userImage, new Callback()
                    {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError(Exception e)
                        {
                            Picasso.get().load(mCurrentUser.getPhotoUrl())
                                    .placeholder(R.drawable.progress_animation)
                                    .into(userImage);
                        }
                    });
        }

        usernameTV.setText(mCurrentUser.getDisplayName());
    }

    private void calculateAverageRating(String location) {

        mDiveSitesDataBase.child(location).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numOfRaters = 0;
                int sumOfRatings = 0;

                if(dataSnapshot.hasChild("rating"))
                {
                    for (DataSnapshot ds :dataSnapshot.child("rating").getChildren()) {
                        sumOfRatings += Integer.parseInt(ds.getValue().toString());
                        numOfRaters++;
                    }

                    mCurrentLocationAvg = (float)sumOfRatings / numOfRaters;
                    mRatingBar.setRating(mCurrentLocationAvg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }

    private void geoLocate() {

        String searchString = mSearchET.getText().toString();

        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {

        }

        mSearchET.setText("");

        if (list.size() > 0) {
            Address address = list.get(0);

            LatLng pos = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, DEFAULT_ZOOM));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.drawer_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.drawer_wish_list:
                startActivity(new Intent(MainActivity.this, WishListActivity.class));
                break;
            case R.id.drawer_my_dives:
                startActivity(new Intent(MainActivity.this, MyDivesActivity.class));
            case R.id.drawer_about:
                break;
            case R.id.drawer_contact:
                break;
            case R.id.drawer_log_out:
                if (mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(29.54721, 34.9539), DEFAULT_ZOOM));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        }

        mDiveSitesDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot d : dataSnapshot.getChildren()){

                    Marker marker;

                    String lat = d.child("lat").getValue().toString();
                    String lng = d.child("lng").getValue().toString();

                    LatLng pos = new LatLng(Float.parseFloat(lat), Float.parseFloat(lng));

                    if(d.child("type").getValue().toString().equals("site")) {

                        marker = mMap.addMarker(new MarkerOptions().position(pos).title(d.getKey()).snippet("site"));
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dive_site_icon));

                        marker.setTag(d.child("location").getValue().toString());

                        mSitesArray.add(marker);
                        marker.setVisible(false);

                    }else if(d.child("type").getValue().toString().equals("club")){

                        marker = mMap.addMarker(new MarkerOptions().position(pos).title(d.getKey()).snippet("club"));
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dive_club_icon));

                        mClubsArray.add(marker);
                        marker.setVisible(false);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        setMarkersListener();
    }

    private void setMarkersListener() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                if(marker.getSnippet().equals("site")) {
                    loadSiteDialog(marker);
                }else if(marker.getSnippet().equals("club")){
                    loadClubDialog(marker);
                }

                return false;
            }
        });
    }

    private void loadSiteDialog(final Marker marker){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.site_dialog, null);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = (TextView) view.findViewById(R.id.site_dialog_title);

        mRatingBar = (RatingBar) view.findViewById(R.id.site_dialog_rating_bar);
        mRatingBar.setClickable(false);

        calculateAverageRating(marker.getTitle());

        Button logNewDiveBtn = (Button) view.findViewById(R.id.site_dialog_add_dive_btn);
        logNewDiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, LogNewDiveActivity.class);
                intent.putExtra("site name", marker.getTitle());
                startActivity(intent);
            }
        });

        Button addToWishList = (Button) view.findViewById(R.id.site_dialog_add_wishlist_btn);
        addToWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mWishlistDatabase.child(mCurrentUser.getUid())
                        .child(marker.getTitle())
                        .setValue(marker.getTag().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,
                                    "This site was added to your wish list",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        Spinner rateSpinner = (Spinner) view.findViewById(R.id.site_rate_spinner);
        ArrayAdapter<CharSequence> rateAdapter = ArrayAdapter
                .createFromResource(MainActivity.this, R.array.rating_array, android.R.layout.simple_spinner_item);

        rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rateSpinner.setAdapter(rateAdapter);

        rateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mDiveSitesDataBase.child(marker.getTitle()).child("rating")
                            .child(mAuth.getCurrentUser().getDisplayName()).setValue(i);
                    calculateAverageRating(marker.getTitle());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        title.setText(marker.getTitle());
        alertDialog.show();
    }

    private void loadClubDialog(final Marker marker){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.club_dialog, null);

        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView titleTV = (TextView) view.findViewById(R.id.club_dialog_title);
        final TextView addressTV = (TextView) view.findViewById(R.id.club_address_text_view);
        final TextView phoneTV = (TextView) view.findViewById(R.id.club_phone_number_text_view);
        final TextView websiteTV = (TextView) view.findViewById(R.id.club_website_text_view);

        phoneTV.setPaintFlags(phoneTV.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        titleTV.setText(marker.getTitle());

        mRatingBar = (RatingBar) view.findViewById(R.id.club_dialog_rating_bar);
        mRatingBar.setClickable(false);

        calculateAverageRating(marker.getTitle());

        Spinner rateSpinner = (Spinner) view.findViewById(R.id.club_rate_spinner);
        ArrayAdapter<CharSequence> rateAdapter = ArrayAdapter
                .createFromResource(MainActivity.this, R.array.rating_array, android.R.layout.simple_spinner_item);

        rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rateSpinner.setAdapter(rateAdapter);

        rateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    mDiveSitesDataBase.child(marker.getTitle()).child("rating")
                            .child(mAuth.getCurrentUser().getDisplayName()).setValue(i);
                    calculateAverageRating(marker.getTitle());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mDiveSitesDataBase.child(marker.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                addressTV.setText(dataSnapshot.child("address").getValue().toString());
                phoneTV.setText(dataSnapshot.child("phone").getValue().toString());
                websiteTV.setText(dataSnapshot.child("url").getValue().toString());

                phoneTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String phone_no = phoneTV.getText().toString().replaceAll("-", "");
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:"+phone_no));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE)
                                == PackageManager.PERMISSION_GRANTED) {

                            startActivity(callIntent);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_PERM)
    private void requestPermissions()
    {
        String[] perms = {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CALL_PHONE };

        if (!EasyPermissions.hasPermissions(this, perms))
        {
            EasyPermissions.requestPermissions(this, "This app needs permissions", RC_PERM, perms);
        }
    }
}
