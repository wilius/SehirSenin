package org.alexiwilius.sehirsenin.domain.map;

import android.os.Handler;

import org.alexiwilius.ranti_app.location.Location;
import org.alexiwilius.ranti_app.util.Timer;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.model.Station;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.ranti_app.util.UIThread;

import java.util.List;

public class StationsUpdater {

    private Location position;

    private NearStationUpdaterCallback callback;
    private Timer timer;
    private Boolean positionUpdated;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            UIThread.run(new Runnable() {
                @Override
                public void run() {
                    if (position == null) return;
                    requestNearStations(position.getLatitude(), position.getLongitude());
                }
            });
        }
    };

    /**
     * sets listener to get updates
     *
     * @param callback indicates the listener
     */
    public StationsUpdater(NearStationUpdaterCallback callback) {
        this.callback = callback;
        timer = new Timer();
    }

    /**
     * sets the current position to get new near stations.
     *
     * @param position current position
     */
    public void setPosition(Location position) {
        if (this.position != null && this.position.distance(position) <= 100) return;

        this.position = position;
        setPositionUpdated(true);
        start(null);
    }

    /**
     * changes current status of the location.
     *
     * @param positionUpdated current status of position to check whether or not updated
     */
    private synchronized void setPositionUpdated(boolean positionUpdated) {
        this.positionUpdated = positionUpdated;
    }

    private void requestNearStations(final Double lat, final Double lng) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyResponse(Database.getNearStations(lat, lng));
                } catch (Exception e) {
                    e.printStackTrace();
                    console.showErrorMessage(UIThread.getActivity(), e.getMessage());
                }
            }
        });
        setPositionUpdated(false);
    }

    /**
     * starts update
     *
     * @param timeout timeout for update
     */
    public void start(Long timeout) {
        if (!timer.isRunning())
            timer.start(runnable, timeout);
    }

    public void start() {
        start(null);
    }

    /**
     * stops update
     */
    public void stop() {
        timer.stop();
    }

    /**
     * notifies new updates to listener
     *
     * @param response contains updated station list
     */
    private void notifyResponse(List<Station> response) {
        callback.onUpdate(response);
        if (positionUpdated)
            start(Param.MAP_STATION_UPDATE_INTERVAL);
    }

    interface NearStationUpdaterCallback {
        void onUpdate(List<Station> data);
    }
}
