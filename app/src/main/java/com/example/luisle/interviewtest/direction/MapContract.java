package com.example.luisle.interviewtest.direction;

import android.location.Location;

import com.example.luisle.interviewtest.BasePresenter;
import com.example.luisle.interviewtest.BaseView;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by LuisLe on 6/30/2017.
 */

public interface MapContract {

    interface View extends BaseView<Presenter>, BaseView.ViewProgress {
        void drawRoutes(LatLng origin, LatLng destination, String originAddress, String destinationAddress, String polylinePoints);
        void showWarningDialog(String message);
        void setToolbarTitle(String title);
    }

    interface Presenter extends BasePresenter {
        void getRoutes(Location currentLocation);
        void setToolbarTitle(String title);
    }

}
