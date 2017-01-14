package br.com.versalius.checkhotel.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by jn18 on 13/01/2017.
 */

public class Preferences {
    // Nome dos arquivos XML
    public static final String USER_PREFERENCES = "user_prefs";

    //Nome das chaves
    public static final String USER_CPF = "user_cpf";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_FIRST_NAME = "user_first_name";
    public static final String USER_ID = "user_id";
    //Esta chave deve ser usada junto com o id da cidade.
    // Ex.: neighborhood_1212 é a chave que guarda o id do último bairro par a cidade de id 1212
    public static final String USER_LAST_NEIGHBORHOOD_OF = "neighborhood_";
    public static final String FORM_LAST_UPDATE = "form_last_update";
    public static final String FORM_PENDING = "form_pending"; //JSONArray com id dos forms pendentes

    private static Preferences instance;
    private SharedPreferences sharedPreferences;
    private Context context;

    private Preferences(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static Preferences getInstance(Context context) {
        if (instance == null) {
            instance = new Preferences(context);
        }
        return instance;
    }

    /**
     * <p>Salva o objeto passado por parâmetro com a chave passada por parâmetro.</p>
     * <p>Se o objeto passado for um {@link HashMap}&lt;{@link String}, {@link String}&gt;, ele será iterado num
     * laço tendo cada um de seus pares inseridos ao {@link SharedPreferences} e o primeiro
     * parâmetro deste método (key) pode ser vazio ou nulo pos as chaves do {@link HashMap} serão utilizadas
     * em seu lugar.</p>
     */
    public void save(String key, Object obj) throws Exception {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            if (obj instanceof HashMap<?, ?>) {
                for (String k : (Set<String>) (((HashMap) obj).keySet())) {
                    String v = (String) ((HashMap) obj).get(k);
                    editor.putString(k, v);
                }
            } else {
                editor.putString(key, String.valueOf(obj));
            }
        } catch (Exception e) {
            throw new CustomException(obj.getClass() + " não suportado para persistência no SharedPreferences.", e);
        }

        editor.commit();
    }

    public String load(String key) {
        return sharedPreferences.getString(key, "");
    }

    /**
     * Remove o valor referente à chave passada.
     *
     * @param key - Chave a qual o valor será removido.
     */
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().commit();
    }
}
