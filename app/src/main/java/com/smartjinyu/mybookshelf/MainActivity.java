package com.smartjinyu.mybookshelf;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container,new BookListFragment())
                .commit();

        final IProfile profile = new ProfileDrawerItem()
                        .withName(getResources().getString(R.string.app_name))
                        .withIcon(R.mipmap.ic_launcher_circle)
                        .withEmail(getResources().getString(R.string.drawer_header_email));

        mAccountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                //.withCompactStyle(true)
                .withHeaderBackground(R.drawable.header)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build();

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(mAccountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_books)
                                .withIcon(R.drawable.ic_bookshelf)
                                .withIdentifier(1)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_search)
                                .withIcon(R.drawable.ic_search)
                                .withIdentifier(2)
                                .withSelectable(false),
                        new SectionDrawerItem()
                            .withName(R.string.drawer_section_label),
                        new PrimaryDrawerItem()
                            .withName(R.string.drawer_item_create_new_label)
                            .withIcon(R.drawable.ic_add)
                            .withIdentifier(3)
                            .withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                            .withName(R.string.drawer_item_settings)
                            .withIcon(R.drawable.ic_settings)
                            .withIdentifier(4)
                            .withSelectable(false),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_about)
                                .withIcon(R.drawable.ic_about)
                                .withIdentifier(5)
                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier()==1){
                                //todo
                            }
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();



    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = mDrawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = mAccountHeader.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //todo
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(mDrawer!=null && mDrawer.isDrawerOpen()){
            mDrawer.closeDrawer();
        }else{
            super.onBackPressed();
        }
    }

}
