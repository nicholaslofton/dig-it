package com.example.root.digit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import text.GLText;

/**
 * Created by nglofton on 7/12/15.
 */
public class TagFragment extends DialogFragment {
    public static final String DATE_ARGS = "date_argument";
    public static final String NAME_ARGS = "name_argument";
    public static final String NOTE_ARGS = "note_argument";
    private Location mLocation;
    private static LocationManager locationManager;
    private String mTitle;
    private String mNote;

    public TagFragment() {

    }

    public TagFragment newInstance(Context context) {

        TagFragment fragment = new TagFragment();

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_tag, null);

        EditText name = (EditText) v.findViewById(R.id.dialog_title);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mTitle = s.toString();
            }
        });
        EditText note = (EditText) v.findViewById(R.id.dialog_tag_data);
        note.setFilters(new InputFilter[] { filter });

        note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {mNote = s.toString();}
        });

        DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (getTargetFragment() != null) {
                    Intent i = new Intent();
                    i.putExtra(NAME_ARGS, mTitle);
                    i.putExtra(NOTE_ARGS, mNote);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                }
                else {
                    Toast.makeText(getActivity().getBaseContext(), "No need to return results.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(v)
                .setTitle("Add Tag")
                .setMessage(Html.fromHtml("<i>Add A Tag<i>"))
                .setPositiveButton("OK", positiveClick);

        return alertDialogBuilder.create();
    }

    public static boolean checkGPS(final Context context) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            return false;
        }
        return true;
    }

    private InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (source.charAt(i) < GLText.CHAR_START || source.charAt(i) > GLText.CHAR_END) {
                    return "";
                }
            }
            return null;
        }
    };;
}
