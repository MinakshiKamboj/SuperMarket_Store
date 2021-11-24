package com.supermarket.store.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.supermarket.store.R;
import com.supermarket.store.fregment.HomeFragment;
import com.supermarket.store.fregment.NotificationFragment;
import com.supermarket.store.fregment.PendingFragment;
import com.supermarket.store.fregment.ProductFragment;
import com.supermarket.store.fregment.ProfileFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.fragment_frame)
    FrameLayout fragmentFrame;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    public boolean callFragment(Fragment fragmentClass) {
        if (fragmentClass != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_frame, fragmentClass).addToBackStack(null).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                break;
            case R.id.navigation_product:
                fragment = new ProductFragment();
                break;
            case R.id.navigation_order:
                fragment = new PendingFragment();
                break;
            case R.id.navigation_notifications:
                fragment = new NotificationFragment();
                break;
            case R.id.navigation_account:
                fragment = new ProfileFragment();
                break;
            default:
                break;
        }
        return callFragment(fragment);
    }
}
