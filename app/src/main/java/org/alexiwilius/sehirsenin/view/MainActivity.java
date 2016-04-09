package org.alexiwilius.sehirsenin.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import org.alexiwilius.ranti_app.location.LocationDetector;
import org.alexiwilius.ranti_app.location.NoActiveLocationSupplier;
import org.alexiwilius.ranti_app.location.NoLocationSupplierException;
import org.alexiwilius.ranti_app.util.Cache;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.ranti_app.view.RantiActivity;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Param;

public class MainActivity extends RantiActivity {

    private MenuDrawer mMenu;
    private WhereIsTheBus mContent;
    private Bundle mOutState;

    private boolean haveAll = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UIThread.isPlayServicesAvailable()) return;

        createMenu();
        setContentView(R.layout.root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().setStatusBarColor(getColor(R.color.claret_red_window));
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(R.color.claret_red_window));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE
            };

            for (String permission : permissions)
                haveAll = haveAll && hasPermission(permission);

            if (!haveAll)
                requestPermissions(permissions, 1);
        }

        if (haveAll)
            init();

    }

    private void init() {
        initContentFragment();

        try {

            // indicates the LocationManager
            LocationDetector.setLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));

            // initiates user's cached cookie data
            Cache.init(this, Param.DATA_IDENTIFIER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !haveAll)
                super.onSaveInstanceState(mOutState);
        } catch (NoLocationSupplierException | NoActiveLocationSupplier e) {
            console.notifyAndClose(this, e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mOutState = outState;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || haveAll)
            super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean haveDenied = false;
        for (int i = 0; i < grantResults.length && !haveDenied; i++)
            haveDenied = grantResults[i] != PackageManager.PERMISSION_GRANTED;
        if (haveDenied)
            console.notifyAndClose(this, getString(R.string.grant_permissions));
        else
            init();
    }

    @Override
    public void onBackPressed() {
        SearchView search = (SearchView) findViewById(R.id.search_view);

        if (mMenu.isMenuVisible())
            mMenu.closeMenu();
        else if (search.clear()) return;
        else if (getFM().getBackStackEntryCount() <= 1)
            finish();
        else
            getFM().popBackStack();
    }

    @Override
    public void finish() {
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void createMenu() {
        mMenu = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, Position.RIGHT, MenuDrawer.MENU_DRAG_WINDOW);
        Display display = getWindowManager().getDefaultDisplay();
        int width;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else
            width = display.getWidth();

        mMenu.setMenuSize(width - 100);
        mMenu.setMenuView(R.layout.map);
        mMenu.openMenu();
    }

    private void initContentFragment() {
        mContent = new WhereIsTheBus();
        getFM().beginTransaction().replace(R.id.container, mContent).commit();
    }

    public void closeMenu() {
        mMenu.closeMenu();
    }

    public void openMenu() {
        mMenu.openMenu();
    }

    public void showLine(String id) {
        Intent intent = new Intent(this, BusActivity.class);
        intent.putExtra(BusActivity.BUS_ID, id);
        startActivity(intent);
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return PackageManager.PERMISSION_GRANTED == checkSelfPermission(permission);
        return PackageManager.PERMISSION_GRANTED == checkCallingOrSelfPermission(permission);
    }
}
