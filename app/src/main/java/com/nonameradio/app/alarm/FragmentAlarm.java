package com.nonameradio.app.alarm;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nonameradio.app.R;
import com.nonameradio.app.NoNameRadioApp;

public class FragmentAlarm extends Fragment implements TimePickerDialog.OnTimeSetListener {
    private RadioAlarmManager ram;
    private ItemAdapterRadioAlarm adapterRadioAlarm;
    private ListView lvAlarms;

    public FragmentAlarm() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NoNameRadioApp app = (NoNameRadioApp)requireActivity().getApplication();
        ram = app.getAlarmManager();

        View view = inflater.inflate(R.layout.layout_alarms, container, false);

        adapterRadioAlarm = new ItemAdapterRadioAlarm(requireActivity());
        lvAlarms = view.findViewById(R.id.listViewAlarms);
        lvAlarms.setAdapter(adapterRadioAlarm);
        lvAlarms.setClickable(true);
        lvAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object anObject = parent.getItemAtPosition(position);
                if (anObject instanceof DataRadioStationAlarm) {
                    ClickOnItem((DataRadioStationAlarm) anObject);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Observe alarms changes using LiveData - automatic lifecycle management
        // Using getViewLifecycleOwner() ensures observer is automatically removed in onDestroyView
        ram.getAlarmsLiveData().observe(getViewLifecycleOwner(), alarms -> {
            RefreshListAndView();
        });
        
        // Initial load
        RefreshListAndView();
    }

    private void RefreshListAndView() {
        adapterRadioAlarm.clear();
        adapterRadioAlarm.addAll(ram.getList());
    }

    DataRadioStationAlarm clickedAlarm = null;
    private void ClickOnItem(DataRadioStationAlarm anObject) {
        clickedAlarm = anObject;
        TimePickerFragment newFragment = new TimePickerFragment(clickedAlarm.hour, clickedAlarm.minute);
        newFragment.setCallback(this);
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ram.changeTime(clickedAlarm.id,hourOfDay,minute);
        view.invalidate();
    }

    public RadioAlarmManager getRam() {
        return ram;
    }
}