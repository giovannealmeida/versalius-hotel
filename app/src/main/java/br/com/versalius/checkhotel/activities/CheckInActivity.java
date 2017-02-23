package br.com.versalius.checkhotel.activities;

import android.app.DatePickerDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.versalius.checkhotel.R;
import br.com.versalius.checkhotel.network.NetworkHelper;
import br.com.versalius.checkhotel.network.ResponseCallback;
import br.com.versalius.checkhotel.utils.CustomSnackBar;
import br.com.versalius.checkhotel.utils.ProgressDialogHelper;
import br.com.versalius.checkhotel.utils.SessionHelper;

public class CheckInActivity extends AppCompatActivity implements OnFocusChangeListener{

    private TextInputLayout tilBookingNumber;

    private EditText etCheckin;
    private EditText etBookingNumber;
    private EditText etObservations;

    private Spinner spVehicle;
    private Spinner spTravel;

    private Spinner spContinentArriving;
    private Spinner spCountryArriving;
    private Spinner spStateArriving;
    private Spinner spCityArriving;
    private Spinner spContinentNext;
    private Spinner spCountryNext;
    private Spinner spStateNext;
    private Spinner spCityNext;

    private ArrayAdapter<String> spCountryArrayAdapterArriving;
    private ArrayList<String> spCountryListDataArriving;
    private HashMap<String, String> countryIdListArriving;
    private ArrayAdapter<String> spCountryArrayAdapterNext;
    private ArrayList<String> spCountryListDataNext;
    private HashMap<String, String> countryIdListNext;

    private ArrayAdapter<String> spStateArrayAdapterArriving;
    private ArrayList<String> spStateListDataArriving;
    private HashMap<String, String> stateIdListArriving;
    private ArrayAdapter<String> spStateArrayAdapterNext;
    private ArrayList<String> spStateListDataNext;
    private HashMap<String, String> stateIdListNext;

    private ArrayAdapter<String> spCityArrayAdapterArriving;
    private ArrayList<String> spCityListDataArriving;
    private HashMap<String, String> cityIdListArriving;
    private ArrayAdapter<String> spCityArrayAdapterNext;
    private ArrayList<String> spCityListDataNext;
    private HashMap<String, String> cityIdListNext;

    private CoordinatorLayout coordinatorLayout;

    private Button btCheckin;

    private Pattern pat;
    private Matcher mat;

    private HashMap<String, String> formData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionHelper.isLogged()) {
            finish();
        } else {
            setContentView(R.layout.activity_check_in);

            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            formData = new HashMap<>();
            setUpViews();
        }
    }

    public void setUpViews(){
         /* Instanciando layout */

        tilBookingNumber = (TextInputLayout) findViewById(R.id.tilBookingNumber);

        /* Instanciando campos */

        etCheckin = (EditText) findViewById(R.id.etCheckIn);
        etBookingNumber = (EditText) findViewById(R.id.etBookingNumber);
        etObservations = (EditText) findViewById(R.id.etObservations);

        /* Instanciando Spinners */

        spVehicle = (Spinner) findViewById(R.id.spVehicle);
        spTravel = (Spinner) findViewById(R.id.spTravel);

        spContinentArriving = (Spinner) findViewById(R.id.spContinentArriving);
        spCountryArriving = (Spinner) findViewById(R.id.spCountryArriving);
        spStateArriving = (Spinner) findViewById(R.id.spStateArriving);
        spCityArriving = (Spinner) findViewById(R.id.spCityArriving);

        spContinentNext = (Spinner) findViewById(R.id.spContinentNext);
        spCountryNext = (Spinner) findViewById(R.id.spCountryNext);
        spStateNext = (Spinner) findViewById(R.id.spStateNext);
        spCityNext = (Spinner) findViewById(R.id.spCityNext);

        /* Instanciando Botão */

        btCheckin = (Button) findViewById(R.id.btCheckin);

        // Adicionando Focus
        etBookingNumber.setOnFocusChangeListener(this);
        etCheckin.setOnFocusChangeListener(this);

        /* Setando título e Back Button na tela de Check in */

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_checkin);

        /* Seta o comportamento do DatePicker */

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar nowCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC-3"));
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etCheckin.setText(dateFormatter.format(newDate.getTime()));
            }

        }, nowCalendar.get(Calendar.YEAR), nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        /* Define a data corrente como o dia selecionável */
        datePickerDialog.getDatePicker().setMaxDate(nowCalendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(nowCalendar.getTimeInMillis());

        etCheckin.setInputType(InputType.TYPE_NULL);
        etCheckin.setText(dateFormatter.format(nowCalendar.getTime()));

        //Abre o Date Picker com click (só funciona se o campo tiver foco)
        etCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        //Abre o Date Picker assim que o campo receber foco
        etCheckin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePickerDialog.show();
            }
        });

        spCountryArriving.setEnabled(false);
        spCountryListDataArriving = new ArrayList<>();
        spCountryListDataArriving.add("Selecione um país...");
        spCountryArrayAdapterArriving = new ArrayAdapter<>(CheckInActivity.this, android.R.layout.simple_spinner_dropdown_item, spCountryListDataArriving);
        spCountryArriving.setAdapter(spCountryArrayAdapterArriving);

        spCountryNext.setEnabled(false);
        spCountryListDataNext = new ArrayList<>();
        spCountryListDataNext.add("Selecione um país...");
        spCountryArrayAdapterNext = new ArrayAdapter<>(CheckInActivity.this, android.R.layout.simple_spinner_dropdown_item, spCountryListDataNext);
        spCountryNext.setAdapter(spCountryArrayAdapterNext);

        spStateArriving.setEnabled(false);
        spStateListDataArriving = new ArrayList<>();
        spStateListDataArriving.add("Selecione um estado...");
        spStateArrayAdapterArriving = new ArrayAdapter<>(CheckInActivity.this, android.R.layout.simple_spinner_dropdown_item, spStateListDataArriving);
        spStateArriving.setAdapter(spStateArrayAdapterArriving);

        spStateNext.setEnabled(false);
        spStateListDataNext = new ArrayList<>();
        spStateListDataNext.add("Selecione um estado...");
        spStateArrayAdapterNext = new ArrayAdapter<>(CheckInActivity.this, android.R.layout.simple_spinner_dropdown_item, spStateListDataNext);
        spStateNext.setAdapter(spStateArrayAdapterNext);

        spCityArriving.setEnabled(false);
        spCityListDataArriving = new ArrayList<>();
        spCityListDataArriving.add("Selecione uma cidade...");
        spCityArrayAdapterArriving = new ArrayAdapter<>(CheckInActivity.this, android.R.layout.simple_spinner_dropdown_item, spCityListDataArriving);
        spCityArriving.setAdapter(spCityArrayAdapterArriving);

        spCityNext.setEnabled(false);
        spCityListDataNext = new ArrayList<>();
        spCityListDataNext.add("Selecione uma cidade...");
        spCityArrayAdapterNext = new ArrayAdapter<>(CheckInActivity.this, android.R.layout.simple_spinner_dropdown_item, spCityListDataNext);
        spCityNext.setAdapter(spCityArrayAdapterNext);

        /*
        ** Carrega Países de um continente (Arriving From)
         */

        spContinentArriving.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /* Carrega o array de ids */
                String[] continent_ids = getResources().getStringArray(R.array.array_continent_id);
                /* Através da posição do estado selecionado no spinner, descobre-se o id dele */
                int selectedContinetId = Integer.valueOf(continent_ids[spContinentArriving.getSelectedItemPosition()]);
                countryIdListArriving = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                if (selectedContinetId == 0) {
                    spCountryListDataArriving.clear();
                    spCountryListDataArriving.add(getResources().getString(R.string.hint_country_spinner));
                    countryIdListArriving.put(getResources().getString(R.string.hint_country_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                    spCountryArriving.setEnabled(false);
                    spCountryArrayAdapterArriving.notifyDataSetChanged();
                    return;
                }

                loadSpCountry(selectedContinetId, spCountryListDataArriving, countryIdListArriving, spCountryArriving, spCountryArrayAdapterArriving);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*
        ** Carrega os estados do país (Arriving From)
         */

        spCountryArriving.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /* Através da posição do estado selecionado no spinner, descobre-se o id dele */
                try {
                    int selectedCountryId = Integer.valueOf(countryIdListArriving.get(spCountryArriving.getSelectedItem().toString()));
                    stateIdListArriving = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                    if (selectedCountryId == 0) {
                        spStateListDataArriving.clear();
                        spStateListDataArriving.add(getResources().getString(R.string.hint_state_spinner));
                        stateIdListArriving.put(getResources().getString(R.string.hint_state_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                        spStateArriving.setEnabled(false);
                        spStateArrayAdapterArriving.notifyDataSetChanged();
                        return;
                    }

                    loadSpState(selectedCountryId, spStateListDataArriving, stateIdListArriving, spStateArriving, spStateArrayAdapterArriving);

                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*
        ** Carrega as cidades do estado (Arriving From)
         */

        spStateArriving.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    int selectedCountryId = Integer.valueOf(stateIdListArriving.get(spStateArriving.getSelectedItem().toString()));
                    cityIdListArriving = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                    if (selectedCountryId == 0) {
                        spCityListDataArriving.clear();
                        spCityListDataArriving.add(getResources().getString(R.string.hint_city_spinner));
                        cityIdListArriving.put(getResources().getString(R.string.hint_city_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                        spCityArriving.setEnabled(false);
                        spCityArrayAdapterArriving.notifyDataSetChanged();
                        return;
                    }

                    loadSpCity(selectedCountryId, spCityListDataArriving, cityIdListArriving, spCityArriving, spCityArrayAdapterArriving);

                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*
        ** Carrega Países de um continente (Next Destination)
         */

        spContinentNext.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /* Carrega o array de ids */
                String[] continent_ids = getResources().getStringArray(R.array.array_continent_id);
                /* Através da posição do estado selecionado no spinner, descobre-se o id dele */
                int selectedContinetId = Integer.valueOf(continent_ids[spContinentNext.getSelectedItemPosition()]);
                countryIdListNext = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                if (selectedContinetId == 0) {
                    spCountryListDataNext.clear();
                    spCountryListDataNext.add(getResources().getString(R.string.hint_country_spinner));
                    countryIdListNext.put(getResources().getString(R.string.hint_country_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                    spCountryNext.setEnabled(false);
                    spCountryArrayAdapterNext.notifyDataSetChanged();
                    return;
                }

                loadSpCountry(selectedContinetId, spCountryListDataNext, countryIdListNext, spCountryNext, spCountryArrayAdapterNext);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*
        ** Carrega os estados do país (Next Destination)
         */

        spCountryNext.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /* Através da posição do estado selecionado no spinner, descobre-se o id dele */
                try {
                    int selectedCountryId = Integer.valueOf(countryIdListNext.get(spCountryNext.getSelectedItem().toString()));
                    stateIdListNext = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                    if (selectedCountryId == 0) {
                        spStateListDataNext.clear();
                        spStateListDataNext.add(getResources().getString(R.string.hint_state_spinner));
                        stateIdListNext.put(getResources().getString(R.string.hint_state_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                        spStateNext.setEnabled(false);
                        spStateArrayAdapterNext.notifyDataSetChanged();
                        return;
                    }

                    loadSpState(selectedCountryId, spStateListDataNext, stateIdListNext, spStateNext, spStateArrayAdapterNext);

                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*
        ** Carrega as cidades do estado (Next Destination)
         */

        spStateNext.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    int selectedCountryId = Integer.valueOf(stateIdListNext.get(spStateNext.getSelectedItem().toString()));
                    cityIdListNext = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                    if (selectedCountryId == 0) {
                        spCityListDataNext.clear();
                        spCityListDataNext.add(getResources().getString(R.string.hint_city_spinner));
                        cityIdListNext.put(getResources().getString(R.string.hint_city_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                        spCityNext.setEnabled(false);
                        spCityArrayAdapterNext.notifyDataSetChanged();
                        return;
                    }

                    loadSpCity(selectedCountryId, spCityListDataNext, cityIdListNext, spCityNext, spCityArrayAdapterNext);

                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(CheckInActivity.this);
                if (NetworkHelper.isOnline(CheckInActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.createProgressSpinner("Aguarde", "Realizando check-in.", true, false);
                        NetworkHelper.getInstance(CheckInActivity.this).checkedIn(Integer.parseInt(etBookingNumber.getText().toString()), new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    Log.v("check-in", String.valueOf(jsonObject));
                                    if (jsonObject.getBoolean("status")) {
                                        CustomSnackBar.make(coordinatorLayout, "O check-in para esse número de reserva já foi realizado", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    } else {
                                        NetworkHelper.getInstance(CheckInActivity.this).checkIn(formData, new ResponseCallback() {
                                            @Override
                                            public void onSuccess(String jsonStringResponse) {
                                                try {
                                                    progressHelper.dismiss();
                                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                                    Log.v("check-in", String.valueOf(jsonObject));
                                                    if (jsonObject.getBoolean("status")) {
                                                        setResult(RESULT_OK, null);
                                                        finish();
                                                    } else {
                                                        CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-in", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFail(VolleyError error) {
                                                progressHelper.dismiss();
                                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-in", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-in", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    } else
                        CustomSnackBar.make(coordinatorLayout, "Atenção! Preencha o formulário corretamente.", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.INFO).show();

                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }
            }
        });
    }

    private boolean isValidForm() {

        // Variável de controle do formulário
        boolean isFocusRequested = false;
        /*
         * Verifica se nenhum veículo foi selecionado, caso positivo, seta o valor do veículo
         * como Outro, caso negativo, seta o veículo selecionado
         */
        if (spVehicle.getSelectedItemPosition() == 0)
            formData.put("vehicle_id", "6");
        else
            formData.put("vehicle_id", String.valueOf(spVehicle.getSelectedItemPosition()));

        /*
         * Verifica se nenhuma viagem foi selecionada, caso positivo, seta o valor da viagem
         * como Outro, caso negativo, seta a viagem selecionada
         */
        if (spTravel.getSelectedItemPosition() == 0)
            formData.put("travel_id", "10");
        else
            formData.put("travel_id", String.valueOf(spTravel.getSelectedItemPosition()));

        // Verifica se o número de reserva foi inserido e se é um número válido
        if (!hasValidBookingNumber()) {
            tilBookingNumber.requestFocus();
            isFocusRequested = true;
        } else
            formData.put("booking_number", etBookingNumber.getText().toString());

        /*
         * Verifica se nenhuma cidade de chegada foi selecionada, caso positivo, seta o valor da cidade
         * como null, caso negativo, seta a viagem selecionada
         */
        if (spCityArriving.getSelectedItemPosition() == 0)
            formData.put("arriving_from_city_id", "");
        else
            formData.put("arriving_from_city_id", cityIdListArriving.get(spCityListDataArriving.get(spCityArriving.getSelectedItemPosition())));

        /*
         * Verifica se nenhuma cidade de chegada foi selecionada, caso positivo, seta o valor da cidade
         * como null, caso negativo, seta a viagem selecionada
         */
        if (spCityNext.getSelectedItemPosition() == 0)
            formData.put("next_destination_city_id", "");
        else
            formData.put("next_destination_city_id",  cityIdListNext.get(spCityListDataNext.get(spCityNext.getSelectedItemPosition())));

        formData.put("observations", etObservations.getText().toString());
        formData.put("checkin", etCheckin.getText().toString());

        return !isFocusRequested;
    }

    private boolean hasValidBookingNumber() {
        String bookingNumber = etBookingNumber.getText().toString().trim();
        pat = Pattern.compile("^[0-9]+$");
        mat = pat.matcher(bookingNumber);

        if (TextUtils.isEmpty(bookingNumber)) {
            tilBookingNumber.setError(getResources().getString(R.string.err_msg_empty_booking_number));
            return false;
        } else if (!mat.find()) {
            tilBookingNumber.setError(getResources().getString(R.string.err_msg_invalid_booking_number));
            return false;
        }

        tilBookingNumber.setErrorEnabled(false);
        return true;
    }

    private void loadSpCountry(int selectedContinetId, final ArrayList<String> spCountryListData,
                               final HashMap<String, String> countryIdList, final Spinner spCountry,
                               final ArrayAdapter<String> spCountryArrayAdapter) {

        final ProgressDialogHelper progressHelper = new ProgressDialogHelper(CheckInActivity.this);
        progressHelper.createProgressSpinner("Aguarde", "Atualizando países", true, false);

        NetworkHelper.getInstance(CheckInActivity.this).getCountries(selectedContinetId, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    spCountryListData.clear();
                    spCountryListData.add(getResources().getString(R.string.hint_country_spinner));
                    countryIdList.put(getResources().getString(R.string.hint_country_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                    JSONArray jArray = new JSONArray(jsonStringResponse);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            spCountryListData.add(jArray.getJSONObject(i).getString("name"));
                            countryIdList.put(jArray.getJSONObject(i).getString("name"), jArray.getJSONObject(i).getString("id"));
                        }
                    }
                    spCountry.setEnabled(true);
                    spCountryArrayAdapter.notifyDataSetChanged();
                    progressHelper.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                Log.i("RESPONSE-FAIL", error.getMessage());
                progressHelper.dismiss();
            }
        });
    }

    private void loadSpState(int selectedCountryId, final ArrayList<String> spStateListData,
                             final HashMap<String, String> stateIdList, final Spinner spState,
                             final ArrayAdapter<String> spStateArrayAdapter) {

        final ProgressDialogHelper progressHelper = new ProgressDialogHelper(CheckInActivity.this);
        progressHelper.createProgressSpinner("Aguarde", "Atualizando estados", true, false);

        NetworkHelper.getInstance(CheckInActivity.this).getStates(selectedCountryId, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    spStateListData.clear();
                    spStateListData.add(getResources().getString(R.string.hint_state_spinner));
                    stateIdList.put(getResources().getString(R.string.hint_state_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                    JSONArray jArray = new JSONArray(jsonStringResponse);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            spStateListData.add(jArray.getJSONObject(i).getString("name"));
                            stateIdList.put(jArray.getJSONObject(i).getString("name"), jArray.getJSONObject(i).getString("id"));
                        }
                    }
                    spState.setEnabled(true);
                    spStateArrayAdapter.notifyDataSetChanged();
                    progressHelper.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                Log.i("RESPONSE-FAIL", error.getMessage());
                progressHelper.dismiss();
            }
        });
    }

    private void loadSpCity(int selectedCountryId, final ArrayList<String> spCityListData,
                            final HashMap<String, String> cityIdList, final Spinner spCity,
                            final ArrayAdapter<String> spCityArrayAdapter) {

        final ProgressDialogHelper progressHelper = new ProgressDialogHelper(CheckInActivity.this);
        progressHelper.createProgressSpinner("Aguarde", "Atualizando cidades", true, false);

        NetworkHelper.getInstance(CheckInActivity.this).getCities(selectedCountryId, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    spCityListData.clear();
                    spCityListData.add(getResources().getString(R.string.hint_city_spinner));
                    cityIdList.put(getResources().getString(R.string.hint_city_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                    JSONArray jArray = new JSONArray(jsonStringResponse);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            spCityListData.add(jArray.getJSONObject(i).getString("name"));
                            cityIdList.put(jArray.getJSONObject(i).getString("name"), jArray.getJSONObject(i).getString("id"));
                        }
                    }
                    spCity.setEnabled(true);
                    spCityArrayAdapter.notifyDataSetChanged();
                    progressHelper.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                Log.i("RESPONSE-FAIL", error.getMessage());
                progressHelper.dismiss();
            }
        });

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus){
            switch (v.getId()){
                case R.id.etBookingNumber:
                    hasValidBookingNumber();
                    break;
            }
        }
    }

    /**
     * NÃO REMOVER DE NOVO!!!!
     * Basicamente seta a ação de fechar a activity ao selecionar a seta na toolbar
     *
     * @param menuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
