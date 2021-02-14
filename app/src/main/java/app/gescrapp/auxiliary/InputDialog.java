package app.gescrapp.auxiliary;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import app.gescrapp.activities.MainActivity;
import app.gescrapp.R;
/**
 * Handles dialog´s interface to insert weight or event duration data
 *  @author Daniel Clemente
 *  @author Jorge García Paredes
 * */
public class InputDialog extends Dialog {
    private Activity act;
    private boolean isWeight, isTime;

    int hours, min = 0;

    public InputDialog(Activity activity, boolean isWeight, boolean isTime) {
        super(activity);
        act = activity;
        this.isWeight = isWeight;
        this.isTime = isTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isWeight) {
            setTitle(R.string.enter_weight_explanation);
            setContentView(R.layout.dialog_weight);

            Button buttonOk = findViewById(R.id.button_dialog_ok);
            Button buttonCancel = findViewById(R.id.button_dialog_dismiss);
            final EditText etWeight = findViewById(R.id.edit_weight);

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String weight = etWeight.getText().toString();
                    if (!weight.equals("")) {
                        Intent i = new Intent(act, MainActivity.class);
                        i.putExtra(MainActivity.KEY_WEIGHT, weight);
                        dismiss();
                        act.finish();
                        act.overridePendingTransition(0, 0);
                        act.startActivity(i);
                        act.overridePendingTransition(0, 0);
                    } else {
                        EditText etWeight = findViewById(R.id.edit_weight);
                        etWeight.setError(act.getText(R.string.error_input));
                    }
                }
            });
        }else {
            if (isTime) {
                setTitle(R.string.enter_time_explanation);
                setContentView(R.layout.dialog_time);

                Button buttonOk = findViewById(R.id.button_dialog_ok);
                Button buttonCancel = findViewById(R.id.button_dialog_dismiss);

                Spinner spinnerH = findViewById(R.id.spinnerH);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapterH = ArrayAdapter.createFromResource(act,
                        R.array.hours_array, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapterH.setDropDownViewResource(R.layout.spinner_list);
                // Apply the adapter to the spinner
                spinnerH.setAdapter(adapterH);

                Spinner spinnerMin = findViewById(R.id.spinnerMin);
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapterMin = ArrayAdapter.createFromResource(act,
                        R.array.min_array, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapterMin.setDropDownViewResource(R.layout.spinner_list);
                // Apply the adapter to the spinner
                spinnerMin.setAdapter(adapterMin);

                spinnerH.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        hours = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        hours = 0;
                    }
                });
                spinnerMin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        min = position*5;       //Shows 5 min intervals
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        min = 0;
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int weight = Integer.getInteger(etWeight.getText().toString());
                        int duration = hours*60 + min;
                        if (duration!=0) {
                            Intent i = new Intent(act, MainActivity.class);
                            i.putExtra(MainActivity.KEY_DURATION_MIN, Integer.toString(duration));
                            dismiss();
                            act.finish();
                            act.overridePendingTransition(0, 0);
                            act.startActivity(i);
                            act.overridePendingTransition(0, 0);
                        } else {
                            TextView tvMin = findViewById(R.id.tv_time_title);
                            tvMin.setError(act.getText(R.string.error_input));
                        }
                    }
                });
            }
        }
    }
}
