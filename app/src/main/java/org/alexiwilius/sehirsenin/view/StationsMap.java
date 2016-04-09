package org.alexiwilius.sehirsenin.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.alexiwilius.ranti_app.android.view.Map;
import org.alexiwilius.ranti_app.location.NoActiveLocationSupplier;
import org.alexiwilius.ranti_app.location.NoLocationSupplierException;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.domain.map.MapController;

public class StationsMap extends Map {

    private Class mControllerClass = null;
    private Marker mPositionMarker = null;

    private MapController mController;
    private ProgressDialog mProgress;

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        initAttrs(activity, attrs);
    }

    private void initAttrs(Activity activity, AttributeSet attrs) {
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.StationsMap);
        if (attrs == null)
            throw new NullPointerException("Attrs couldn't null. ");

        try {
            setControllerClass(Class.forName(a.getString(R.styleable.StationsMap_controller)));
        } catch (ClassNotFoundException e) {
            console.showErrorMessage(getActivity(), e.getMessage());
        }
        a.recycle();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        start();

        if (mPositionMarker != null) {
            restoreLastState();
        }
    }

    @Override
    public void onDestroyView() {
        stop();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onPause() {

        stop();
        super.onPause();

    }

    private void stop() {
        try {
            if (mController != null)
                mController.stop();
        } catch (NoLocationSupplierException e) {
            console.notifyAndClose(getActivity(), e.getMessage());
        }
    }

    public void start() {
        try {
            if (mController == null)
                mController = (MapController) mControllerClass.getConstructor(StationsMap.class).newInstance(this);
            mController.start();
        } catch (Exception e) {
            console.showErrorMessage(getActivity(), e.getMessage());
        }

    }

    public void showProgress() {
        mProgress = new ProgressDialog(this.getActivity());
        mProgress.setMessage(getActivity().getString(R.string.got_location));
        mProgress.show();
    }

    public void closeProgress() {
        if (mProgress != null && mProgress.isShowing())
            mProgress.dismiss();
    }

    public LatLng getPosition() {
        if (!isUserLocationGot())
            return null;

        return mPositionMarker.getPosition();
    }

    public float getRotation() throws Exception {
        if (!isUserLocationGot())
            throw new Exception(getActivity().getString(R.string.location_not_detected));

        return mPositionMarker.getRotation();

    }

    public Marker getCurrentPosMarker() {
        return mPositionMarker;
    }

    public void setPosition(LatLng latLng, boolean move) {
        if (isUserLocationGot()) {
            mPositionMarker.setPosition(latLng);
            if (move)
                moveCameraToCurrentPosition(false);
        }
    }

    public void setRotation(float rotation) {
        if (isUserLocationGot())
            mPositionMarker.setRotation(rotation);
    }

    public void setCurrentPosMarker(Marker currentPosMarker) {
        this.mPositionMarker = currentPosMarker;
    }

    public boolean isUserLocationGot() {
        return mPositionMarker != null;
    }

    /**
     * @param lngLtd specifies marker's current position
     */
    public Marker addMarker(LatLng lngLtd, Integer resourceId, String title) {
        MarkerOptions markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.5f, 0.5f)
                .position(lngLtd);

        if (resourceId != null)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(resourceId));

        if (title != null)
            markerOptions.title(title);

        return getMap().addMarker(markerOptions);
    }

    /**
     * @param animate specifies that movement will be animated or not
     */
    public void moveCameraToCurrentPosition(boolean animate) {
        LatLng position = getPosition();
        if (position != null)
            moveCameraToPosition(position, animate);
    }

    /**
     * @param animate specifies that movement will be animated or not
     */
    public void moveCameraToPosition(LatLng pos, boolean animate) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pos, mController.getZoom());
        if (animate)
            getMap().animateCamera(update);
        else
            getMap().moveCamera(update);
    }

    /**
     * {@link #moveCameraToCurrentPosition(boolean)}  }
     */
    public void moveCameraToCurrentPosition() {
        moveCameraToCurrentPosition(false);
    }

    /**
     * restores users last state slily so camera movement made without animate.
     */
    private synchronized void restoreLastState() {
        if (isUserLocationGot()) {
            addPositionMarker(mPositionMarker.getPosition());
            moveCameraToCurrentPosition(false);
        }
    }

    public void addPositionMarker(LatLng position) {
        setCurrentPosMarker(addMarker(position, R.drawable.position_indicator, getActivity().getString(R.string.my_location)));
    }

    public void onMarkerClick(GoogleMap.OnMarkerClickListener listener) {
        getMap().setOnMarkerClickListener(listener);
    }

    public void setControllerClass(Class controllerClass) {
        this.mControllerClass = controllerClass;
    }

    public MapController getController() {
        return mController;
    }

    public Polyline addPolyline(PolylineOptions options) {
        return getMap().addPolyline(options);
    }
}