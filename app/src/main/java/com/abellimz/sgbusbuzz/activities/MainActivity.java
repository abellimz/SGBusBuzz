package com.abellimz.sgbusbuzz.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.database.BusStopQueryHelper;
import com.abellimz.sgbusbuzz.fragments.FavouritesFragment;
import com.abellimz.sgbusbuzz.fragments.GuideFragment;
import com.abellimz.sgbusbuzz.fragments.NearbyFragment;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.services.BusBuzzService;
import com.abellimz.sgbusbuzz.utils.AnimUtils;
import com.abellimz.sgbusbuzz.utils.BusFormatUtils;
import com.abellimz.sgbusbuzz.utils.ViewUtils;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GuideFragment.BusStopSearchInterface, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    public static final String KEY_EXTRA_AWARE_BUS_STOPS = "AWARE BUS STOPS";
    private static final String TAG_FAV_FRAG = "Favourites";
    private static final String TAG_NEARBY_FRAG = "Nearby";
    private static final String TAG_GUIDE_FRAG = "Guide";
    private static final int PERMISSION_CODE_LOCATION = 100;
    private static final int PERMISSION_CODE_LOCATION_AWARENESS = 150;
    private static final int INDEX_NAV_FAV = 0;
    private static final int INDEX_NAV_NEARBY = 1;
    private static final int INDEX_NAV_GUIDE = 2;
    private final int LIMIT_QUERY = 30;
    private final int RADIUS_NEARBY = 500;
    @BindColor(R.color.colorPrimary)
    public int colorPrimary;
    @BindView(R.id.container_main)
    FrameLayout mainContainer;
    @BindView(R.id.map_view_container)
    CardView mapContainer;
    @BindView(R.id.main_overlay)
    FrameLayout overlay;
    @BindView(R.id.fab_map_nearby)
    FloatingActionButton mapFab;
    @BindView(R.id.main_toolbar)
    Toolbar toolbar;
    @BindView(R.id.bottom_nav_main)
    AHBottomNavigation bottomNavigation;
    @BindString(R.string.nav_title_fav)
    String navTitleFav;
    @BindString(R.string.nav_title_nearby)
    String navTitleNearby;
    @BindString(R.string.nav_title_guide)
    String navTitleGuide;
    @BindString(R.string.map_current_location)
    String currentLocText;
    @BindColor(R.color.colorFavourites)
    int colorFav;
    @BindColor(R.color.colorNearby)
    int colorNearby;
    @BindColor(R.color.colorGuide)
    int colorGuide;
    @BindDrawable(R.drawable.ic_heart_filled)
    Drawable iconFav;
    @BindDrawable(R.drawable.ic_gps)
    Drawable iconNearby;
    @BindDrawable(R.drawable.ic_book)
    Drawable iconGuide;
    private List<BusStop> mFavourites;
    private ResultCallback<LocationResult> mLocationCallback;
    private List<BusStop> mapBusStops;
    private LatLng currentLatLng;
    private Marker lastClickedMarker;
    private boolean isMapOpen;
    private boolean shouldMoveCamera = true;

    private BusStopQueryHelper mBusStopHelper;
    private GoogleApiClient mGoogleClient;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupGoogleApi();
        setupBottomNav();
        setupFab();
        setupSearch();
    }

    private void setupGoogleApi() {
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleClient.connect();
    }

    private void setupFab() {
        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NearbyFragment frag = ((NearbyFragment) getSupportFragmentManager()
                        .findFragmentByTag(TAG_NEARBY_FRAG));
                if (frag == null) {
                    return;
                }
                List<BusStop> busStops = frag.getBusStops();
                showMap((ArrayList<BusStop>) busStops);
            }
        });
        mapFab.setTranslationY(-ViewUtils.dpToPx(56));
    }

    private void setupBottomNav() {
        setupNavItems();
        setupNavStyle();
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (wasSelected) {
                    refreshTab(position);
                } else {
                    openTab(position);
                }
                return true;
            }
        });
        // Hack to get favourites to open by default
        openTab(INDEX_NAV_FAV);
    }

    private void setupSearch() {
        mBusStopHelper = new BusStopQueryHelper(this, LIMIT_QUERY, RADIUS_NEARBY);
    }

    private void setupNavItems() {
        AHBottomNavigationItem favItem = new AHBottomNavigationItem(
                navTitleFav, iconFav, colorFav
        );
        AHBottomNavigationItem nearbyItem = new AHBottomNavigationItem(
                navTitleNearby, iconNearby, colorNearby
        );
        AHBottomNavigationItem guideItem = new AHBottomNavigationItem(
                navTitleGuide, iconGuide, colorGuide
        );

        bottomNavigation.addItem(favItem);
        bottomNavigation.addItem(nearbyItem);
        bottomNavigation.addItem(guideItem);
    }

    private void setupNavStyle() {
        bottomNavigation.setBehaviorTranslationEnabled(true);
        bottomNavigation.setAccentColor(colorPrimary);
//        bottomNavigation.setColored(true);
    }

    private void refreshTab(int position) {
        switch (position) {
            case INDEX_NAV_FAV:
                FavouritesFragment navFrag = (FavouritesFragment)
                        getSupportFragmentManager().findFragmentByTag(TAG_FAV_FRAG);
                navFrag.refreshTab();
                break;
            case INDEX_NAV_NEARBY:
                NearbyFragment nearbyFrag = (NearbyFragment)
                        getSupportFragmentManager().findFragmentByTag(TAG_NEARBY_FRAG);
                nearbyFrag.refreshTab();
                break;
            case INDEX_NAV_GUIDE:
                GuideFragment guideFrag = (GuideFragment)
                        getSupportFragmentManager().findFragmentByTag(TAG_GUIDE_FRAG);
                break;
        }
    }

    private void openTab(int position) {
        switch (position) {
            case INDEX_NAV_FAV:
                openFavourites();
                break;
            case INDEX_NAV_NEARBY:
                openNearby();
                break;
            case INDEX_NAV_GUIDE:
                openGuide();
                break;
            default:
                break;
        }
    }

    private void openFavourites() {
        mapFab.hide(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, new FavouritesFragment(), TAG_FAV_FRAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void openNearby() {
        mapFab.show(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, new NearbyFragment(), TAG_NEARBY_FRAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void openGuide() {
        mapFab.hide(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_main, new GuideFragment(), TAG_GUIDE_FRAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleClient;
    }

    public void registerFencesWithPerms(List<BusStop> favourites) {
        mFavourites = favourites;
        requestLocationPermission(PERMISSION_CODE_LOCATION_AWARENESS, null);
    }

    @SuppressWarnings("MissingPermission")
    private void registerFences() {
        ArrayList<BusStop> awareBusStops = new ArrayList<>();
        for (BusStop busStop : mFavourites) {
            if (busStop.isAware()) {
                awareBusStops.add(busStop);
            }
        }
        if (awareBusStops.isEmpty()) {
            resetIntent();
            return;
        }
        startBusBuzzService(awareBusStops);
    }

    private void startBusBuzzService(ArrayList<BusStop> awareBusStops) {
        Intent intent = resetIntent();
        intent.putParcelableArrayListExtra(KEY_EXTRA_AWARE_BUS_STOPS, awareBusStops);
        startService(intent);
    }

    private Intent resetIntent() {
        Intent intent = new Intent(this, BusBuzzService.class);
        stopService(intent);
        return intent;
    }

    public void getUserLocation(ResultCallback<LocationResult> callback) {
        mLocationCallback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestLocationPermission(PERMISSION_CODE_LOCATION, callback);
        } else {
            getLocation(callback);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLocation(ResultCallback<LocationResult> callback) {
        GoogleApiClient client = getGoogleApiClient();
        Awareness.SnapshotApi.getLocation(client)
                .setResultCallback(callback);
    }

    public void getSuggestions(String query, BusStopQueryHelper.QueryListener listener) {
        mBusStopHelper.getSuggestions(query, listener);
    }

    public void findNearby(double lat, double lon, BusStopQueryHelper.QueryListener listener) {
        mBusStopHelper.findNearby(lat, lon, listener);
    }

    public void getFavourites(BusStopQueryHelper.QueryListener listener) {
        mBusStopHelper.getFavourites(listener);
    }

    public void removeFavourite(BusStop busStop, BusStopQueryHelper.EditListener listener) {
        mBusStopHelper.removeFromFavourite(busStop, listener);
    }

    public void addFavourite(BusStop busStop, BusStopQueryHelper.EditListener listener) {
        mBusStopHelper.addToFavourite(busStop, listener);
    }

    public void setAwareness(BusStop busStop, boolean isAware, BusStopQueryHelper.EditListener listener) {
        mBusStopHelper.setAwareness(busStop, isAware, listener);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermission(int code, ResultCallback<LocationResult> callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    code);
        } else {
            processPermissionCode(code);
        }
    }

    private void processPermissionCode(int code) {
        switch (code) {
            case PERMISSION_CODE_LOCATION:
                getLocation(mLocationCallback);
                return;
            case PERMISSION_CODE_LOCATION_AWARENESS:
                registerFences();
                return;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isMapOpen) {
            closeMap(mapFab);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            processPermissionCode(requestCode);
        }
        //TODO: Add dialog to say permission not granted
    }

    public void closeMap(View view) {
        if (!isMapOpen) {
            return;
        }
        isMapOpen = false;
        AnimUtils.animateCloseMap(mapFab, mapContainer, overlay, null);
    }

    public void closeMap(Animator.AnimatorListener listener) {
        if (!isMapOpen) {
            return;
        }
        isMapOpen = false;
        AnimUtils.animateCloseMap(mapFab, mapContainer, overlay, listener);
    }

    public void showMap(final ArrayList<BusStop> busStops) {
        if (busStops == null) {
            return;
        }
        mapBusStops = busStops;
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        getUserLocation(new ResultCallback<LocationResult>() {
            @Override
            public void onResult(@NonNull final LocationResult locationResult) {
                isMapOpen = true;
                Location location = locationResult.getLocation();
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AnimUtils.animateOpenMap(mapFab, mapContainer, overlay, new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mapFragment.getMapAsync(MainActivity.this);
                        }
                    });
                } else {
                    mapContainer.setVisibility(View.VISIBLE);
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (currentLatLng == null || mapBusStops == null) {
            return;
        }
        map.setOnInfoWindowClickListener(this);
        map.setOnMarkerClickListener(this);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(false);
        map.clear();
        if (shouldMoveCamera) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    currentLatLng, 18));
            shouldMoveCamera = false;
        }
        Marker myMarker = map.addMarker(new MarkerOptions().position(currentLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(currentLocText));
        myMarker.showInfoWindow();
        for (int i = 0; i < mapBusStops.size(); i++) {
            BusStop busStop = mapBusStops.get(i);
            LatLng location = new LatLng(busStop.getLatitude(),
                    busStop.getLongitude());
            String dist = BusFormatUtils.getFormattedDistance(busStop.getDistance());
            Marker addedMarker = map.addMarker(new MarkerOptions()
                    .position(location)
                    .title(busStop.getDescription() + " (" +
                            dist + " away)"));
            addedMarker.setTag(i);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Integer position = ((Integer) marker.getTag());
        if (position == null) {
            return;
        }
        showTiming(position);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer position = ((Integer) marker.getTag());
        if (position == null) {
            lastClickedMarker = marker;
            return false;
        }
        if (lastClickedMarker != null && lastClickedMarker.getTag() != null
                && lastClickedMarker.getTag().equals(position)) {
            showTiming(position);
            return true;
        }
        lastClickedMarker = marker;
        return false;
    }

    private void showTiming(final int position) {
        closeMap(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                switch (bottomNavigation.getCurrentItem()) {
                    case INDEX_NAV_FAV:
                        break;
                    case INDEX_NAV_NEARBY:
                        NearbyFragment frag = (NearbyFragment) getSupportFragmentManager().findFragmentByTag(TAG_NEARBY_FRAG);
                        frag.showTimingAtPosition(position);
                        break;
                    case INDEX_NAV_GUIDE:
                        break;
                }
            }
        });
    }
}
