package com.example.fyp.mechanica;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.fyp.mechanica.helpers.Constants;
import com.example.fyp.mechanica.models.User;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class BaseDrawerActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private FrameLayout view_stub;
    private NavigationView navigation_view;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Menu drawerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);
        view_stub = (FrameLayout) findViewById(R.id.view_stub);
        navigation_view = (NavigationView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

//        mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.actionBarText));

        navigation_view.inflateMenu(R.menu.menu_drawer_item_list);
        View view =  navigation_view.inflateHeaderView(R.layout.nav_header);
        CircleImageView civUserPhoto = view.findViewById(R.id.civ_user_photo);
        TextView tvUserName = view.findViewById(R.id.tv_username);
        TextView tvUserRole = view.findViewById(R.id.tv_user_role);

        User currentUser = Paper.book().read(Constants.CURR_USER_KEY);
        tvUserName.setText(currentUser.name);

        if (currentUser.userRole.equals("Customer")) {
            tvUserRole.setText("Customer");

        } else {
            tvUserRole.setText("Mechanic");
        }
//        if (currentUser.roleId == 0) {
//            navigation_view.inflateMenu(R.menu.player_drawer_menu_items);
//
//        } else {
//            navigation_view.inflateMenu(R.menu.club_drawer_menu_items);
//        }
//
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerMenu = navigation_view.getMenu();
        for(int i = 0; i < drawerMenu.size(); i++) {
            drawerMenu.getItem(i).setOnMenuItemClickListener(this);
        }
        // and so on...
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Override all setContentView methods to put the content view to the FrameLayout view_stub
     * so that, we can make other activity implementations looks like normal activity subclasses.
     */
    @Override
    public void setContentView(int layoutResID) {
        if (view_stub != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            View stubView = inflater.inflate(layoutResID, view_stub, false);
            view_stub.addView(stubView, lp);
        }
    }

    @Override
    public void setContentView(View view) {
        if (view_stub != null) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            view_stub.addView(view, lp);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (view_stub != null) {
            view_stub.addView(view, params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        // set item as selected to persist highlight
        item.setChecked(true);

        switch (item.getItemId()) {
            case R.id.menu_drawer_item_history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;

            case R.id.menu_drawer_item_vehicle_detail:
                startActivity(new Intent(this, VehicleDetailActivity.class));
                break;

            case R.id.menu_drawer_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
//
//            case R.id.menu_drawer_item_about:
//                startActivity(new Intent(this, AboutActivity.class));
//                break;

            case R.id.menu_drawer_item_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Paper.book().delete(Constants.CURR_USER_KEY);
                startActivity(intent);
                finish();

        }

        return false;
    }
}
