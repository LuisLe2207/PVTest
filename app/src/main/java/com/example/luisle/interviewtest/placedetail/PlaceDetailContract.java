package com.example.luisle.interviewtest.placedetail;

import android.support.annotation.NonNull;

import com.example.luisle.interviewtest.BasePresenter;
import com.example.luisle.interviewtest.BaseView;
import com.example.luisle.interviewtest.data.Place;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by LuisLe on 6/28/2017.
 */

public interface PlaceDetailContract {

    interface View extends BaseView<Presenter>, BaseView.ViewProgress {
        void showPlaces();
        void showPlaceEditUi(@NonNull String placeID);
        void setPlaceOnMap(LatLng latLng, String placeName);
        void setData(@NonNull Place place);
        void showDeleteAlertDlg();
        void showWarningDialog(String message);
        void startDirectionActivity(@NonNull String placeID);
    }

    interface Presenter extends BasePresenter {
        void deletePlace();
        void findRoute();
        void populatePlace();
        void openEditPlaceUi();
        void openDeleteAlertDlg();
    }

}
