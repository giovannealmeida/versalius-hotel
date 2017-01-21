package br.com.versalius.checkhotel.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.versalius.checkhotel.utils.EncryptHelper;

/**
 * Created by jn18 on 13/01/2017.
 */
public class NetworkHelper {
    private static final String TAG = NetworkHelper.class.getSimpleName();

    private static NetworkHelper instance;
    private static Context context;
    private RequestQueue requestQueue;

    private final String DOMINIO = "http://checkhotel.versalius.com.br/api"; // Remoto
//    private final String DOMINIO = "http://192.168.1.106/akijob/api"; // Repo

    private final String LOGIN = "/login_controller";
    private final String SIGNUP = "/user_controller/update";
    private final String FORGOT_PASSWORD = "/login_controller/forgot_password_send_hash";
    private final String CHECK_EMAIL = "/user_controller/email_check";
    private final String CHECK_CPF = "/user_controller/cpf_check";
    private final String SEND_SURVEY = "/survey";
    private final String COUNTRY = "/countries_controller";
    private final String STATE = "/states_controller";
    private final String CITY = "/cities_controller";
    private final String USER = "/user_controller";


    private NetworkHelper(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Pegar o contexto da aplicação garante que a requestQueue vai ser singleton e só
            // morre quando a aplicação parar
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    //Retorna uma instância estática de NetworkHelper
    public static synchronized NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    public void doLogin(String cpf, String password, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("cpf", cpf);
        params.put("password", EncryptHelper.SHA1(password));
        execute(Request.Method.POST, params, TAG, DOMINIO + LOGIN, callback);
    }

    public void getCountries(long continent_id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("continent_id", String.valueOf(continent_id));

        execute(Request.Method.GET,
                null, //GET não precisa de parâmetro no corpo
                TAG,
                buildGetURL(DOMINIO + COUNTRY, params),
                callback);
    }

    public void getStates(long country_id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("country_id", String.valueOf(country_id));

        execute(Request.Method.GET,
                null, //GET não precisa de parâmetro no corpo
                TAG,
                buildGetURL(DOMINIO + STATE, params),
                callback);
    }

    public void getCities(long stateId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("state_id", String.valueOf(stateId));

        execute(Request.Method.GET,
                null, //GET não precisa de parâmetro no corpo
                TAG,
                buildGetURL(DOMINIO + CITY, params),
                callback);
    }

    public void doSignUp(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + SIGNUP,
                callback);
    }

    public void userUpdate(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + SIGNUP,
                callback);
    }

    public void forgotPassword(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + FORGOT_PASSWORD,
                callback);
    }

    public void login(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST,
                params,
                TAG,
                DOMINIO + LOGIN,
                callback);
    }

    public void getUser(int userId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));

        execute(Request.Method.GET,
                null, //GET não precisa de parâmetro no corpo
                TAG,
                buildGetURL(DOMINIO + USER, params),
                callback);
    }

    /**
     * Verifica se o e-mail já existe.
     * <p>
     * TODO: Verificar o funcionamento desse controller (???)
     * Testes realizados com os parâmetros (email e id existem no banco e estão relacionados):
     * email_check?email=aphodyty_7@hotmail.com&user_id=108
     * <p>
     * Se somente um email é passado, dá erro.
     * Se um email e um id de usuário que existem no banco são passados, retorna 'false'
     * Se um email que não existe no banco e um id de usuário que existe são passados, retorna 'false'
     * Se um email que existe no banco e um id de usuário que não existe são passados, retorna 'true'
     *
     * @param email   - Email e id do usuário
     * @param callback
     */
    public void emailExists(String email, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);

        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + CHECK_EMAIL, params),
                callback);
    }

    public void cpfExists(String cpf, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("cpf", cpf);

        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + CHECK_CPF, params),
                callback);
    }

    private void execute(int method, final HashMap params, String tag, String url, final ResponseCallback callback) {
        final CustomRequest request = new CustomRequest(
                method,
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("onResponse - LOG", "response: " + response);
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("onResponse - LOG", "response: " + error.getMessage());
                        if (callback != null) {
                            callback.onFail(error);
                        }
                    }
                });

        request.setTag(tag);
        getRequestQueue().add(request);
    }

    private String buildGetURL(String url, HashMap<String, String> params) {
        url += "?";
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            url += pair.getKey() + "=" + pair.getValue();
            it.remove(); // avoids a ConcurrentModificationException
            if (it.hasNext()) {
                url += "&";
            }
        }
        return url;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
