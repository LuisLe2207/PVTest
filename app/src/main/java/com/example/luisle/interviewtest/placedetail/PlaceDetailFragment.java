package com.example.luisle.interviewtest.placedetail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.luisle.interviewtest.AppModule;
import com.example.luisle.interviewtest.MyApp;
import com.example.luisle.interviewtest.R;
import com.example.luisle.interviewtest.addeditplace.AddEditPlaceFragment;
import com.example.luisle.interviewtest.data.Place;
import com.example.luisle.interviewtest.direction.DirectionActivity;
import com.example.luisle.interviewtest.places.PlacesFragment;
import com.example.luisle.interviewtest.utils.AppUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.makeramen.roundedimageview.RoundedImageView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.luisle.interviewtest.utils.AppUtils.ADD_EDIT_FRAGMENT_TAG;
import static com.example.luisle.interviewtest.utils.AppUtils.LANDSCAPE_STACK;
import static com.example.luisle.interviewtest.utils.AppUtils.PLACE_FRAGMENT_TAG;
import static com.example.luisle.interviewtest.utils.AppUtils.PORTRAIT_STACK;

/**
 * Created by LuisLe on 6/28/2017.
 */

public class PlaceDetailFragment extends Fragment implements PlaceDetailContract.View, OnMapReadyCallback{

    @Inject
    PlaceDetailPresenter placeDetailPresenter;

    private PlaceDetailContract.Presenter presenter;
    private AppUtils.Communicator communicator;

    @BindView(R.id.imgDetailFrag)
    RoundedImageView placeImage;

    @BindView(R.id.edtDetailFrag_PlaceName)
    EditText edtPlaceName;

    @BindView(R.id.edtDetailFrag_PlaceAddress)
    EditText edtPlaceAddress;

    @BindView(R.id.edtDetailFrag_PlaceDescription)
    EditText edtPlaceDescription;

    @BindView(R.id.mvDetailFrag_Map)
    MapView mapView;


    private GoogleMap googleMap;

    private ProgressDialog progressDialog;

    private boolean deviceIsLandscapeTablet = false;

    public static final String PLACE_ID = "PlaceID";

    private String placeID;

    public static PlaceDetailFragment newInstance(@NonNull String placeID) {
        PlaceDetailFragment placeDetailFragment = new PlaceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PLACE_ID, placeID);
        placeDetailFragment.setArguments(bundle);

        return placeDetailFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        communicator = (AppUtils.Communicator) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getContext().getResources().getString(R.string.txt_delete_dialog));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);

        deviceIsLandscapeTablet = AppUtils.deviceIsTabletAndInLandscape(getActivity());

        placeID = getArguments().getString(PLACE_ID);

        DaggerPlaceDetailPresenterComponent.builder()
                .appModule(new AppModule(getContext()))
                .placeDetailPresenterModule(new PlaceDetailPresenterModule(this, placeID))
                .placesRepositoryComponent(((MyApp) getActivity().getApplication()).getRepositoryComponent()).build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_detail, container, false);
        ButterKnife.bind(this, root);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        communicator.setActionBarTitle(getContext().getResources().getString(R.string.action_bar_title_detail));
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @OnClick(R.id.ibtnDetailFrag_Edit)
    public void editPlace(View view) {
        presenter.openEditPlaceUi();
    }

    @OnClick(R.id.ibtnDetailFrag_Delete)
    public void deletePlace(View view) {
        presenter.openDeleteAlertDlg();
    }

    @OnClick(R.id.ibtnDetailFrag_Direction)
    public void getDirection(View view) {
        presenter.findRoute();
    }

    @Override
    public void setPresenter(PlaceDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showProgressDlg() {
        progressDialog.show();
    }

    @Override
    public void hideProgressDlg() {
        progressDialog.hide();
    }

    @Override
    public void showPlaces() {

        PlacesFragment placesFragment = (PlacesFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PLACE_FRAGMENT_TAG);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if (deviceIsLandscapeTablet) {
            AddEditPlaceFragment addEditPlaceFragment = AddEditPlaceFragment.newInstance(null);
            transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            transaction.replace(R.id.mainAct_AnotherFragContent, addEditPlaceFragment, ADD_EDIT_FRAGMENT_TAG);
        } else {
            // Pop Places Fragment out of BackStack
            getActivity().getSupportFragmentManager().popBackStack();
            transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
            transaction.replace(R.id.mainAct_FrameLayout, placesFragment, PLACE_FRAGMENT_TAG);
        }
        transaction.commit();
        placesFragment.onResume();
    }

    @Override
    public void showPlaceEditUi(@NonNull String placeID) {
        AddEditPlaceFragment addEditPlaceFragment = AddEditPlaceFragment.newInstance(placeID);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.slide_in, R.anim.slide_out);

        if (!deviceIsLandscapeTablet) {
            transaction.replace(R.id.mainAct_FrameLayout, addEditPlaceFragment, ADD_EDIT_FRAGMENT_TAG).addToBackStack(PORTRAIT_STACK);
        } else {
            transaction.replace(R.id.mainAct_AnotherFragContent, addEditPlaceFragment, ADD_EDIT_FRAGMENT_TAG).addToBackStack(LANDSCAPE_STACK);
        }

        transaction.commit();
    }

    @Override
    public void setPlaceOnMap(LatLng latLng, String placeName) {
        float zoom = 15;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(placeName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        Marker marker = googleMap.addMarker(markerOptions);
        marker.showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void setData(@NonNull Place place) {
        byte[] imgByte = place.getPlaceImage();
        if (imgByte != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            placeImage.setImageBitmap(bitmap);
        } else {
            placeImage.setImageResource(R.mipmap.ic_no_image);
        }

        edtPlaceName.setText(place.getPlaceName());
        edtPlaceAddress.setText(place.getPlaceAddress());
        edtPlaceDescription.setText(place.getPlaceDescription());
    }

    @Override
    public void showDeleteAlertDlg() {
        final AlertDialog.Builder alertDialog =  new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getContext().getResources().getString(R.string.txt_delete_alert_dialog_title));
        alertDialog.setMessage(getContext().getResources().getString(R.string.txt_delete_alert_dialog) + " '" + edtPlaceName.getText() + "'");
        alertDialog.setIcon(R.mipmap.ic_warning);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.deletePlace();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setCancelable(true).create().show();
    }

    @Override
    public void showWarningDialog(String message) {
        final AlertDialog.Builder alertDialog =  new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getContext().getResources().getString(R.string.txt_warning_alert_dialog_title));
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.mipmap.ic_warning);
        alertDialog.setCancelable(true).create().show();
    }

    @Override
    public void startDirectionActivity(@NonNull String placeID) {
        Intent directionActIntent = new Intent(getActivity(), DirectionActivity.class);
        directionActIntent.putExtra(PLACE_ID, placeID);
        startActivity(directionActIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setAllGesturesEnabled(false);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
    }
}
