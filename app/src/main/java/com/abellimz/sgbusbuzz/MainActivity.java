package com.abellimz.sgbusbuzz;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.bottom_nav_main)
    AHBottomNavigation bottomNavigation;

    @BindView(R.id.container_main)
    FrameLayout mainContainer;

    @BindString(R.string.nav_title_fav)
    String navTitleFav;

    @BindString(R.string.nav_title_nearby)
    String navTitleNearby;

    @BindString(R.string.nav_title_guide)
    String navTitleGuide;

    @BindColor(R.color.colorFavourites)
    int colorFav;

    @BindColor(R.color.colorNearby)
    int colorNearby;

    @BindColor(R.color.colorGuide)
    int colorGuide;

    @BindDrawable(R.drawable.ic_heart)
    Drawable iconFav;

    @BindDrawable(R.drawable.ic_gps)
    Drawable iconNearby;

    @BindDrawable(R.drawable.ic_book)
    Drawable iconGuide;

    @BindColor(R.color.colorPrimary)
    int colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupBottomNav();
    }

    private void setupBottomNav(){
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

        bottomNavigation.setColored(true);
    }
}
