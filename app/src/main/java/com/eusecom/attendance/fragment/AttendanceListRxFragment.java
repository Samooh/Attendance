package com.eusecom.attendance.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eusecom.attendance.Constants;
import com.eusecom.attendance.SettingsActivity;
import com.eusecom.attendance.animators.BaseItemAnimator;
import com.eusecom.attendance.animators.FadeInAnimator;
import com.eusecom.attendance.animators.FadeInDownAnimator;
import com.eusecom.attendance.animators.FadeInLeftAnimator;
import com.eusecom.attendance.animators.FadeInRightAnimator;
import com.eusecom.attendance.animators.FadeInUpAnimator;
import com.eusecom.attendance.models.Attendance;
import com.eusecom.attendance.rxbus.RxBus;
import com.eusecom.attendance.rxfirebase2.database.RxFirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.eusecom.attendance.R;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import rx.Subscriber;

//by https://github.com/ezhome/Android-RxFirebase

public class AttendanceListRxFragment extends Fragment {

    private static final String TAG = "AttendanceListRxFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    public AttendanceListRxFragment() {}
    private ProgressDialog fProgressDialog;
    boolean isCancelable;
    String timestampx;

    private CompositeDisposable _disposables;

    enum Type {
        FadeIn(new FadeInAnimator()),
        FadeInDown(new FadeInDownAnimator()),
        FadeInUp(new FadeInUpAnimator()),
        FadeInLeft(new FadeInLeftAnimator()),
        FadeInRight(new FadeInRightAnimator());

        private BaseItemAnimator mAnimator;

        Type(BaseItemAnimator animator) {
            mAnimator = animator;
        }

        public BaseItemAnimator getAnimator() {
            return mAnimator;
        }
    }

    private AttendanceRxAdapter mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    public GetAttendanceSubscriber getAttendanceSubscriber;
    private final DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference();
    private RxBus _rxBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String githubToken = Constants.ETEST_API_KEY;
        String urlx = SettingsActivity.getServerName(getActivity());

        _disposables = new CompositeDisposable();

        _rxBus = getRxBusSingleton();

        ConnectableFlowable<Object> tapEventEmitter = _rxBus.asFlowable().publish();

        _disposables
                .add(tapEventEmitter.subscribe(event -> {
                    if (event instanceof AttendanceListRxFragment.TapEvent) {
                        ///_showTapText();
                    }
                    if (event instanceof Attendance) {
                        String keys = ((Attendance) event).getRok();
                        Log.d("In FRGM longClick", keys);
                        getAttendanceDialog( keys, (Attendance) event);

                    }
                }));

        _disposables
                .add(tapEventEmitter.publish(stream ->
                        stream.buffer(stream.debounce(1, TimeUnit.SECONDS)))
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(taps -> {
                            ///_showTapCount(taps.size()); OK
                        }));

        _disposables.add(tapEventEmitter.connect());
    }

    public RxBus getRxBusSingleton() {
        if (_rxBus == null) {
            _rxBus = new RxBus();
        }

        return _rxBus;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_attendances, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.attendances_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new AttendanceRxAdapter(Collections.<Attendance>emptyList(), _rxBus);
        getAttendanceSubscriber = new GetAttendanceSubscriber();

        DatabaseReference gettimestramp = FirebaseDatabase.getInstance().getReference("gettimestamp");
        gettimestramp.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                //System.out.println(dataSnapshot.getValue());
                timestampx=dataSnapshot.getValue().toString();
                //Log.d(TAG, "ServerValue.TIMESTAMP " + timestampx);

            }

            public void onCancelled(DatabaseError databaseError) { }
        });
        gettimestramp.setValue(ServerValue.TIMESTAMP);


        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setItemAnimator(new FadeInRightAnimator());
        mRecycler.getItemAnimator().setAddDuration(300);
        mRecycler.getItemAnimator().setRemoveDuration(300);
        loadAttendances();


    }//end of onActivityCreated

    private void loadAttendances() {

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query recentAbsencesQuery = firebaseRef.child("user-attendances").child(userId).orderByChild("datm");

        showfProgressDialog();
        RxFirebaseDatabase.getInstance().observeValueEvent(recentAbsencesQuery).subscribe(getAttendanceSubscriber);
    }

    private void renderAttendanceList(List<Attendance> blogPostEntities) {
        //showProgress(false);
        hidefProgressDialog();
        mAdapter.setData(blogPostEntities);
        //Log.d("blogPostEntities", blogPostEntities.get(0).getTitle());
        //Log.d("blogPostEntities", blogPostEntities.get(1).getTitle());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        _disposables.dispose();
        if (getAttendanceSubscriber != null && !getAttendanceSubscriber.isUnsubscribed()) {
            getAttendanceSubscriber.unsubscribe();
        }

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }




    private void getAttendanceDialog(String postkey, Attendance model) {

        //if savetofir > 0 then save to server
        String savetofir =  SettingsActivity.getFir(getActivity());
        int savetofiri = Integer.parseInt(savetofir);

        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.absences_dialog);
        dialog.setTitle(R.string.item);
        // set the custom dialog components - text, image and button
        long timestampod = Long.parseLong(model.daod) * 1000L;
        String dateods = getDate(timestampod );

        String textx = model.usname + " " + model.dmxa +  " " + model.dmna + " " + dateods;
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText(textx);
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_image_edit);

        Button buttonPositive = (Button) dialog.findViewById(R.id.buttonPositive);
        // if button is clicked, close the custom dialog
        buttonPositive.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dialog.dismiss();

                Toast.makeText(getActivity(), getResources().getString(R.string.cantdelitem), Toast.LENGTH_SHORT).show();

            }
        });
        Button buttonNegative = (Button) dialog.findViewById(R.id.buttonNegative);
        // if button is clicked, close the custom dialog
        buttonNegative.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dialog.dismiss();

                Toast.makeText(getActivity(), getResources().getString(R.string.cantedititem), Toast.LENGTH_SHORT).show();

            }
        });
        dialog.show();

    }//end getdialog



    private String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }


    public void showfProgressDialog() {
        if (fProgressDialog == null) {
            fProgressDialog = new ProgressDialog(getActivity());
            fProgressDialog.setCancelable(isCancelable);
            fProgressDialog.setMessage("Loading...");
        }

        fProgressDialog.show();
    }

    public void hidefProgressDialog() {
        if (fProgressDialog != null && fProgressDialog.isShowing()) {
            fProgressDialog.dismiss();
        }
    }




    /**
     * Subscriber for {@link //RxFirebaseDatabase} query
     */
    private final class GetAttendanceSubscriber extends Subscriber<DataSnapshot> {
        @Override public void onCompleted() {
            //showProgress(false);
            hidefProgressDialog();
            getAttendanceSubscriber.unsubscribe();
        }

        @Override public void onError(Throwable e) {
            //showProgress(false);
            //showError(e.getMessage());
            hidefProgressDialog();
            getAttendanceSubscriber.unsubscribe();
        }

        @SuppressWarnings("unchecked") @Override public void onNext(DataSnapshot dataSnapshot) {
            List<Attendance> blogPostEntities = new ArrayList<>();
            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                String keys = childDataSnapshot.getKey();
                Log.d("keys ", keys);
                Attendance resultx = childDataSnapshot.getValue(Attendance.class);
                resultx.setRok(keys);
                blogPostEntities.add(resultx);
            }
            renderAttendanceList(blogPostEntities);

        }
    }//end of getAbsenceSubscriber

    public static class TapEvent {}


}
