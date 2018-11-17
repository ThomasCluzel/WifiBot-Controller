package com.pizz.wifibotcontroller;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by bemunoz2 on 27/01/2017.
 */

/**
 * This class handles the screen for the gyroscopic mode
 */
public class GyroscopFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new View(getActivity());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float width = v.getWidth();

                Intent intent = new Intent(getActivity(), ControllerService.class);

                intent.putExtra("screenCurrentlyTouched",
                        event.getAction()==MotionEvent.ACTION_DOWN ||
                                event.getAction()==MotionEvent.ACTION_MOVE);

                if(x<width/2){
                    intent.putExtra("leftPressed", true);
                    intent.putExtra("typeTreatment",TreatmentGyroscop.TREATMENT_GYROSCOP);
                }
                else{
                    intent.putExtra("rightPressed", true);
                    intent.putExtra("typeTreatment",TreatmentGyroscop.TREATMENT_GYROSCOP);
                }
                getActivity().startService(intent);

                return true;
            }
        });
        return view;
    }
}
