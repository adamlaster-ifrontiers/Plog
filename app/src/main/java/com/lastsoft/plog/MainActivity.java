package com.lastsoft.plog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.PopupMenu;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.List;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        AddGroupFragment.OnFragmentInteractionListener,
        AddPlayFragment.OnFragmentInteractionListener,
        AddPlayerFragment.OnFragmentInteractionListener,
        AddGameFragment.OnFragmentInteractionListener,
        PlaysFragment.OnFragmentInteractionListener,
        PlayersFragment.OnFragmentInteractionListener,
        GamesFragment.OnFragmentInteractionListener,
        ViewPlayFragment.OnFragmentInteractionListener,
        View.OnClickListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(41943040)
                .diskCacheSize(104857600)
                .threadPoolSize(10)
                .build();
        ImageLoader.getInstance().init(config);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                mDrawerLayout);



        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        LoadGamesTask initDb = new LoadGamesTask();
        try {
            initDb.execute();
        } catch (Exception e) {

        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        //Log.d("V1", ""+position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0){
            //int backStackCount = manager.getBackStackEntryCount();
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            //fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragUp = false;
        }
        if (position == 2){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new PlaysFragment(), "plays")
                    .commit();
        }else if (position == 1){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new PlayersFragment(), "players")
                    .commit();
        }else if (position == 0){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new GamesFragment(), "games")
                    .commit();
        }else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                    .commit();
        }
    }





    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section3);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section1);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        //if(mTitle.equals(getString(R.string.title_section3))) actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {
        if (id.contains("refresh_players")){
            onSectionAttached(3);
            PlayersFragment playersFrag = (PlayersFragment)
                    getSupportFragmentManager().findFragmentByTag("players");
            if (playersFrag != null) {
                playersFrag.refreshDataset();
            }
        }else if (id.contains("refresh_games")){
            GamesFragment collectionFrag = (GamesFragment)
                    getSupportFragmentManager().findFragmentByTag("collection");
            if (collectionFrag != null) {
                collectionFrag.refreshDataset();
            }
        }else if (id.contains("refresh_plays")){
            PlaysFragment playsFrag = (PlaysFragment)
                    getSupportFragmentManager().findFragmentByTag("plays");
            if (playsFrag != null) {
                playsFrag.refreshDataset();
            }
        }
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Floating Action Clicked in " + mTitle, Toast.LENGTH_SHORT).show();
    }

    public void onListItemClicked(String id){
        Toast.makeText(this, id + " List Item Clicked", Toast.LENGTH_SHORT).show();
    }

    public void onPlayClicked(Play clickedPlay, Fragment mFragment, View view, View nameView, View dateView){
        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){}

        mFragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
        mFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        mViewPlayFragment = ViewPlayFragment.newInstance(clickedPlay.getId(),view.getTransitionName(), nameView.getTransitionName(), dateView.getTransitionName());
        mViewPlayFragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
        mViewPlayFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
        if (view != null) {
            ft.addSharedElement(view, view.getTransitionName());
        }
        ft.addSharedElement(nameView, nameView.getTransitionName());
        ft.addSharedElement(dateView, dateView.getTransitionName());
        ft.replace(R.id.container, mViewPlayFragment, "view_play");
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions(); //Prevents the flashing.
    }

    public void openAddPlay(String game_name, long playID){


        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){}



        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragUp = true;
        mAddPlayFragment = AddPlayFragment.newInstance((int) 0, (int) 0, true, game_name, playID);
        ft.add(R.id.container, mAddPlayFragment, "add_play");
        ft.addToBackStack("add_play");
        ft.commit();

        mTitle = game_name;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //System.gc();
                //Log.d("V1", "background status allocated=" + Long.toString(Debug.getNativeHeapAllocatedSize()));
                //Log.d("V1", "background status free=" + Long.toString(Debug.getNativeHeapFreeSize()));
                GamesFragment collectionFrag = (GamesFragment)
                        getSupportFragmentManager().findFragmentByTag("games");
                if (collectionFrag != null) {
                    collectionFrag.clearQuery();
                    collectionFrag.refreshDataset();
                }
            }
        }, 1000);

    }

    public void deletePlay(long playID){
        Play deleteMe = Play.findById(Play.class, playID);

        //delete PlayersPerPlay
        List<PlayersPerPlay> players = PlayersPerPlay.getPlayers(deleteMe);
        for(PlayersPerPlay player:players){
            player.delete();
        }
        //delete GamesPerPay
        List<GamesPerPlay> games = GamesPerPlay.getGames(deleteMe);
        for(PlayersPerPlay player:players){
            player.delete();
        }

        //delete play image
        File deleteImage = new File(deleteMe.playPhoto);
        if (deleteImage.exists()) deleteImage.delete();

        //delete play
        deleteMe.delete();

        onFragmentInteraction("refresh_plays");
    }

    private Boolean fragUp = false;
    private AddPlayerFragment mAddPlayerFragment;
    private AddGameFragment mAddGameFragment;
    private AddPlayFragment mAddPlayFragment;
    private AddGroupFragment mAddGroupFragment;
    private ViewPlayFragment mViewPlayFragment;
    @Override
    public void onFragmentInteraction(String id, float x, float y) {
        //Log.d("V1", "x = " + x);
        //Log.d("V1", "y = " + y);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (id.equals("add_player")) {
            fragUp = true;
            mAddPlayerFragment = AddPlayerFragment.newInstance((int) x, (int) y, true);
            ft.add(R.id.container, mAddPlayerFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();
        }else if (id.equals("add_play")) {
            //no longer used
            /*fragUp = true;
            mAddPlayFragment = AddPlayFragment.newInstance((int) x, (int) y, true, "", -1);
            ft.add(R.id.container, mAddPlayFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();*/
        }else if (id.equals("add_group")) {
            fragUp = true;
            mAddGroupFragment = AddGroupFragment.newInstance((int) x, (int) y, true);
            ft.add(R.id.container, mAddGroupFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();
        }else if (id.equals("add_game")) {
            fragUp = true;
            mAddGameFragment = AddGameFragment.newInstance((int) x, (int) y, true);
            ft.add(R.id.container, mAddGameFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();
        }
        //Log.d("V1", id);
    }
    @Override
    public void onBackPressed(){
        if(fragUp){
            if (mAddPlayerFragment != null) removeFragment(mAddPlayerFragment.getView());
            if (mAddGameFragment != null) removeFragment(mAddGameFragment.getView());
            if (mAddPlayFragment != null)removeFragment(mAddPlayFragment.getView());
            if (mAddGroupFragment != null) removeFragment(mAddGroupFragment.getView());
        }else{
            super.onBackPressed();
        }
    }

    /*
    Called by the back button in fragment_main.xml
     */
    public void removeFragment(View v){
        fragUp = false;
        if (mAddPlayerFragment != null){
            mAddPlayerFragment.removeYourself();
            mAddPlayerFragment = null;
        }
        if (mAddGameFragment != null){
            mAddGameFragment.removeYourself();
            mAddGameFragment = null;
        }
        if (mAddPlayFragment != null){
            mAddPlayFragment.removeYourself();
            mAddPlayFragment = null;
        }
        if (mAddGroupFragment != null){
            mAddGroupFragment.removeYourself();
            mAddGroupFragment = null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
