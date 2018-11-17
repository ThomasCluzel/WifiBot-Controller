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
 * Created by bemunoz2 on 20/03/2017.
 */

public class JoystickFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new View(getActivity());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                float width = v.getWidth();
                float height = v.getHeight();

                Intent intent = new Intent(getActivity(), ControllerService.class);

                intent.putExtra("screenCurrentlyTouched",
                        event.getAction() == MotionEvent.ACTION_DOWN ||
                                event.getAction() == MotionEvent.ACTION_MOVE);

                intent.putExtra("xPosNorm", x/width);
                intent.putExtra("yPosNorm", y/height);
                intent.putExtra("typeTreatment", TreatmentJoystick.TREATMENT_JOYSTICK);
                getActivity().startService(intent);

                return true;
            }
        });
        return view;
    }
}

