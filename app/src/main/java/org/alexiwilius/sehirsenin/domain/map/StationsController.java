package org.alexiwilius.sehirsenin.domain.map;

import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.v4.app.NavUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.alexiwilius.ranti_app.location.Location;
import org.alexiwilius.ranti_app.location.LocationDetector;
import org.alexiwilius.ranti_app.location.NoActiveLocationSupplier;
import org.alexiwilius.ranti_app.location.NoLocationSupplierException;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.model.Station;
import org.alexiwilius.sehirsenin.view.StationsMap;
import org.json.JSONException;

import java.util.List;

/**
 * Created by AlexiWilius on 4.1.2015.
 */
public class StationsController extends MapController implements LocationDetector.LocationResult,
        StationsUpdater.NearStationUpdaterCallback {

    @IntDef({Station.NEAR_STATION, Station.LINE_PATH_STATION})
    private @interface StationTypes {
    }

    private StationsUpdater updater;
    private OnStationSelectListener onStationSelectListener;
    private List<Station> stations;

    public StationsController(final StationsMap map) {
        super(map);
        map.onMarkerClick(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (Station station : stations)
                    if (station.getMarker().equals(marker)) {
                        fireOnSelect(station);
                        break;
                    }
                return false;
            }
        });
        updater = new StationsUpdater(this);

        if (location == null)
            map.showProgress();
    }

    public void addStations(List<Station> stations, @StationTypes Integer type) throws JSONException {
        clearStations();
        this.stations = stations;

        for (Station station : stations) {
            station.setMarker(
                    map.addMarker(
                            station.getLocation().getPosition(),
                            R.drawable.next_bus32,
                            station.getName()));

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param location sets the position of current position marker
     * @throws Exception
     */
    public void setCurrentPosition(Location location) throws Exception {
        updater.setPosition(location);
        rotateIndicator(location);
        map.setPosition(location.getPosition(), false);
    }

    /**
     * register the fragment to listen location updates
     */
    public void registerLocationListener() throws NoActiveLocationSupplier, NoLocationSupplierException {
        LocationDetector.registerListener(this);
    }

    /**
     * remove listener that listen location updates
     */
    public void removeLocationListener() {
        LocationDetector.removeListener(this);
    }

    /**
     * rotates position indicator as users movement direction
     *
     * @param location last obtained position of the device
     */
    public void rotateIndicator(final Location location) {
        try {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();

            final double startRotation = map.getRotation();
            final long duration = 500;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);

                    float rotation = (float) (t * location.getBearing() + (1 - t)
                            * startRotation);

                    map.setRotation(rotation);

                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * called when NearStationUpdater got new station list around the current position
     *
     * @param stations contains update response
     */
    @Override
    public void onUpdate(List<Station> stations) {
        try {
            addStations(stations, Station.NEAR_STATION);
        } catch (Exception e) {
            console.showErrorMessage(map.getActivity(), e.getMessage());
        }

    }

    /**
     * called when a new location updates obtained
     *
     * @param loc last obtained location
     */
    @Override
    public void gotLocation(final Location loc) {
        super.gotLocation(loc);
        UIThread.run(new Runnable() {
            @Override
            public void run() {
                LatLng lngLtd = new LatLng(loc.getLatitude(), loc.getLongitude());

                // this means it is first location got.
                if (map.getCurrentPosMarker() == null) {
                    map.addPositionMarker(lngLtd);
                    map.closeProgress();
                    // to handle users location updates after first location got
                    map.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition) {
                            setZoom(cameraPosition.zoom);
                        }
                    });
                    map.moveCameraToCurrentPosition();
                }

                float distance = loc.distance(map.getCurrentPosMarker().getPosition());

                try {
                    setCurrentPosition(loc);

                    if (distance > 250)
                        map.moveCameraToCurrentPosition(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void locationDisabled() {
        if (location == null)
            console.notifyAndClose(map.getActivity(), map.getActivity().getString(R.string.gps_required));
    }


    public void setOnStationSelectListener(OnStationSelectListener onStationSelectListener) {
        this.onStationSelectListener = onStationSelectListener;
    }

    public void clearStations() {
        if (stations != null)
            for (Station station : stations)
                station.setMarker(null);
    }

    public void stop() {
        removeLocationListener();
        updater.stop();
    }

    public void start() throws NoLocationSupplierException, NoActiveLocationSupplier {
        registerLocationListener();
        updater.start();
    }

    private void fireOnSelect(Station station) {
        if (onStationSelectListener == null) return;

        onStationSelectListener.onSelect(station);
    }

    public interface OnStationSelectListener {
        void onSelect(Station station);
    }
}