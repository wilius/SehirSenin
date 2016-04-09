package org.alexiwilius.sehirsenin.domain.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.alexiwilius.ranti_app.location.Location;
import org.alexiwilius.ranti_app.location.LocationDetector;
import org.alexiwilius.ranti_app.network.RequestTask;
import org.alexiwilius.ranti_app.network.request.PostRequest;
import org.alexiwilius.ranti_app.network.request.Request;
import org.alexiwilius.ranti_app.network.request.SOAPContent;
import org.alexiwilius.ranti_app.network.response.ErrorResponse;
import org.alexiwilius.ranti_app.network.response.JSONResponse;
import org.alexiwilius.ranti_app.network.response.Response;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.sehirsenin.res.SOAPTemplate;
import org.alexiwilius.sehirsenin.view.BusActivity;
import org.alexiwilius.sehirsenin.view.StationsMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class PathController extends MapController implements LocationDetector.LocationResult {
    List<POJO> stationList = new ArrayList<>();
    private Polyline polyline;

    int counter = 1;
    private boolean isFirst = true;
    Float mZoom = 14f;
    private boolean isFirstNearest = true;

    public PathController(StationsMap map) {
        super(map);
        loadPath(0);
        map.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                setZoom(cameraPosition.zoom);
            }
        });
        if (location == null)
            map.showProgress();
        else
            gotLocation(location);

    }

    @Override
    public void gotLocation(final Location location) {
        super.gotLocation(location);
        map.closeProgress();
        UIThread.run(new Runnable() {
            @Override
            public void run() {
                if (isFirst) {
                    isFirst = false;
                    map.addPositionMarker(location.getPosition());
                    showPath();
                }
                map.setPosition(location.getPosition(), false);
            }
        });
    }

    @Override
    public void locationDisabled() {
        if (location == null)
            console.notifyAndClose(map.getActivity(), map.getActivity().getString(R.string.gps_required));
    }

    private void showPath() {
        dec();
        if (counter != 0) return;
        try {
            PolylineOptions options = getPolylineOption();
            LatLng nearest = null,
                    current = location.getPosition();
            Float min = Float.MAX_VALUE, calc;
            POJO busStop;
            for (int i = 0; i < stationList.size(); i++) {
                busStop = stationList.get(i);

                busStop.marker = map.addMarker(busStop.pos, R.drawable.next_bus32, busStop.name);

                if ((calc = Location.distance(busStop.pos, current)) < min) {
                    min = calc;
                    nearest = busStop.pos;
                }

                options.add(busStop.pos);
            }

            polyline = map.addPolyline(options);
            if (nearest != null && isFirstNearest) {
                isFirstNearest = false;
                map.moveCameraToPosition(nearest, false);
            }
        } catch (Exception e) {
            console.notifyAndClose(map.getActivity(), e.getMessage());
        }
    }

    private PolylineOptions getPolylineOption() {
        PolylineOptions options = new PolylineOptions();
        options.color(map.getResources().getColor(R.color.claret_red_window));
        options.width(5);
        options.visible(true);
        return options;
    }

    @Override
    public float getZoom() {
        return mZoom;
    }

    @Override
    public void setZoom(float zoom) {
        mZoom = zoom;
    }

    protected void loadPath(int direction) {
        try {
            String lineId = map.getActivity().getIntent().getStringExtra(BusActivity.BUS_ID);
            inc();
            mTask.cancel();
            mTask.execute(
                    new PostRequest(Param.SERVICE_URL,
                            new SOAPContent(SOAPTemplate.getLinePath(lineId, direction),
                                    Param.SOAPActions.LINE_PATH_LIST)));
        } catch (Exception e) {
            console.notifyAndClose(map.getActivity(), e.getMessage());
        }
    }

    private void clear() {
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }
        POJO obj;
        for (int i = 0; i < stationList.size(); i++) {
            obj = stationList.get(i);
            if (obj.marker != null)
                stationList.get(i).marker.remove();
        }
        stationList.clear();
    }

    private RequestTask<Request, JSONResponse> mTask = new RequestTask<>(new RequestTask.ResponseReady() {
        @Override
        protected void onSuccess(Response responses) {
            clear();
            try {
                NodeList list = ((Document) responses.getData()).getElementsByTagName("a:Durak"), list2;
                Element item;
                POJO pojo;
                String lat, lng;
                for (int i = 0; i < list.getLength(); i++) {
                    list2 = list.item(i).getChildNodes();
                    pojo = new POJO();
                    lat = lng = null;
                    for (int j = 0; j < list2.getLength(); j++) {
                        item = (Element) list2.item(j);
                        String nodeName = item.getNodeName();

                        if (nodeName.contains("Adi"))
                            pojo.name = item.getTextContent();
                        else if (nodeName.contains("Id"))
                            pojo.id = item.getTextContent();
                        else if (nodeName.contains("KoorX"))
                            lng = item.getTextContent();
                        else if (nodeName.contains("KoorY"))
                            lat = item.getTextContent();
                    }
                    pojo.pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    stationList.add(pojo);
                }
            } catch (Exception e) {
                console.showErrorMessage(map.getContext(), e.getMessage());
            }
        }

        @Override
        protected void onError(ErrorResponse responses) {
            clear();
            console.notifyAndClose(map.getActivity(), responses.getData().toString());
        }

        @Override
        protected void onCancel() {
            dec();
        }

        @Override
        protected void onComplete(Response responses) {
            showPath();
        }
    });

    public void setDirection(int i) {
        loadPath(i);
    }

    private synchronized void inc() {
        counter++;
    }

    private synchronized void dec() {
        counter--;
    }

    private class POJO {
        String id, name;
        public LatLng pos;
        Marker marker;
    }
}