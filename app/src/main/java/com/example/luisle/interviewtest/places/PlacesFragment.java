package com.example.luisle.interviewtest.places;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.luisle.interviewtest.MyApp;
import com.example.luisle.interviewtest.R;
import com.example.luisle.interviewtest.adapter.PlacesAdapter;
import com.example.luisle.interviewtest.addeditplace.AddEditPlaceFragment;
import com.example.luisle.interviewtest.data.Place;
import com.example.luisle.interviewtest.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.luisle.interviewtest.utils.AppUtils.ADD_EDIT_FRAGMENT_TAG;

/**
 * Created by LuisLe on 6/28/2017.
 */

public class PlacesFragment extends Fragment implements PlacesContract.View, PlacesAdapter.OnClickCallback{

    @Inject
    PlacesPresenter placesPresenter;

    private PlacesContract.Presenter presenter;
    private AppUtils.Communicator communicator;

    @BindView(R.id.txtListFrag_NoData)
    TextView txtNoData;

    @BindView(R.id.fabListFrag_AddPlace)
    FloatingActionButton fabAddPlace;

    @BindView(R.id.rcvListFrag_Places)
    RecyclerView rcvPlaces;

    private PlacesAdapter placesAdapter;

    private boolean deviceIsLanscapeTablet = false;

    public static PlacesFragment newInstance() {
        return new PlacesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        communicator = (AppUtils.Communicator) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placesAdapter = new PlacesAdapter(getContext(), new ArrayList<Place>(0), this);

        deviceIsLanscapeTablet = AppUtils.deviceIsTabletAndInLandscape(getActivity());

        DaggerPlacesPresenterComponent.builder()
                .placesRepositoryComponent(((MyApp)getActivity().getApplication()).getRepositoryComponent())
                .placesPresenterModule(new PlacesPresenterModule(this)).build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_place_list, container, false);
        ButterKnife.bind(this, root);

        // Check Device Size and Orientation
        if (deviceIsLanscapeTablet) {
            // Device is tablet and in landscape mode
            fabAddPlace.setVisibility(View.INVISIBLE);
        } else {
            // Device is tablet(portrait) or phones(both portrait and landscape)
            fabAddPlace.setVisibility(View.VISIBLE);
            rcvPlaces.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0 && fabAddPlace.getVisibility() == View.VISIBLE) {
                        fabAddPlace.hide();
                    } else if (dy < 0 && fabAddPlace.getVisibility() != View.VISIBLE) {
                        fabAddPlace.show();
                    }
                }
            });
        }

        rcvPlaces.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvPlaces.setAdapter(placesAdapter);

        fabAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               presenter.addNewPlace();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.start();
        }
        communicator.setActionBarTitle(getContext().getResources().getString(R.string.action_bar_title_list));
    }

    @Override
    public void setPresenter(PlacesContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showPlaces(List<Place> places) {
        placesAdapter.replaceData(places);
    }

    @Override
    public void showAddPlaceUi() {
        if (!deviceIsLanscapeTablet) {
            AddEditPlaceFragment addEditPlaceFragment = AddEditPlaceFragment.newInstance(null);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.slide_in, R.anim.slide_out);
            transaction.replace(R.id.mainAct_FrameLayout, addEditPlaceFragment, ADD_EDIT_FRAGMENT_TAG).addToBackStack(null).commit();
        }
    }

    @Override
    public void showNoDataAvailable() {
        txtNoData.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoDataAvailable() {
        txtNoData.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showPlaceDetailUI(@NonNull String placeID) {

    }

    @Override
    public void startDirectionActivity(View v, Place place) {

    }
}
