package org.alexiwilius.sehirsenin.domain.map;

import org.alexiwilius.ranti_app.location.Location;
import org.alexiwilius.ranti_app.location.LocationDetector;
import org.alexiwilius.ranti_app.location.NoActiveLocationSupplier;
import org.alexiwilius.ranti_app.location.NoLocationSupplierException;
import org.alexiwilius.ranti_app.util.Cache;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.sehirsenin.view.StationsMap;

/**
 * Created by AlexiWilius on 17.1.2015.
 */
public abstract class MapController implements LocationDetector.LocationResult {


    protected final StationsMap map;
    protected static Location location;

    public MapController(StationsMap map) {
        this.map = map;
    }

    public void start() throws NoLocationSupplierException, NoActiveLocationSupplier {
        LocationDetector.registerListener(this);
    }

    public void stop() throws NoLocationSupplierException {
        LocationDetector.removeListener(this);
    }

    /**
     * restores users last zoom state
     *
     * @param zoom user's last zoom ratio
     */
    public void setZoom(float zoom) {
        try {
            Cache.set(Param.ARG_ZOOM_AMOUNT, zoom);
        } catch (Exception ignored) {
        }
    }

    /**
     * @return zoom ratio preserved. if there is no ratio preserved, by default get it as 15.0f
     */
    public float getZoom() {
        return (Float) Cache.get(Param.ARG_ZOOM_AMOUNT, 15.0f);
    }


    @Override
    public void gotLocation(Location location) {
        this.location = location;
    }
}
