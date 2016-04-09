package org.alexiwilius.sehirsenin.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import org.alexiwilius.ranti_app.location.Location;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.ReachedMaxStarredCardException;

/**
 * Created by AlexiWilius-WS on 7.12.2015.
 */
public class Station {
    public static final int NEAR_STATION = 0;
    public static final int LINE_PATH_STATION = 1;

    private final String id;

    private Location loc;
    private Marker marker;
    private String name;
    private boolean favorite;

    public Station(String id, String name, double lat, double lng, boolean favorite) {
        this.id = id;
        this.name = name;
        this.loc = Location.create(lat, lng);
        this.favorite = favorite;
    }

    public void setMarker(Marker marker) {
        if (marker == null && this.marker != null)
            this.marker.remove();

        this.marker = marker;
        updateMarkerImage();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) throws ReachedMaxStarredCardException {
        Database.changeStationStarredStatus(id, favorite ? Database.STARRED_STATION : Database.NORMAL_STATION);
        this.favorite = favorite;
        updateMarkerImage();
    }

    public Location getLocation() {
        return loc;
    }

    public Object getMarker() {
        return marker;
    }

    private void updateMarkerImage() {
        if (this.marker == null) return;
        if (isFavorite())
            this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.next_bus_starred32));
        else
            this.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.next_bus32));
    }
}
