package com.fourtic.paranimf.entradas.rest;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.constants.Constants;
import com.fourtic.paranimf.entradas.data.Butaca;
import com.fourtic.paranimf.entradas.data.Evento;
import com.fourtic.paranimf.entradas.data.ResponseEventos;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

@Singleton
public class RestService
{
    // private static String BASE_SECURE_URL = "http://192.168.3.102:8081/par-public/rest";
    private static String BASE_SECURE_URL = "http://ujiapps.uji.es/par-public/rest";
    
    private static final String API_KEY = "kajshdka234hsdoiuqhiu918092";

    private AsyncHttpClient client;
    private Gson gson;
    private Context context;

    @Inject
    public RestService(Application application)
    {
        this.context = application;
        this.client = new AsyncHttpClient();
        this.gson = new Gson();

        client.setTimeout(10000);
        initCookieStore(context);
        initSsl();
    }

    private void initSsl()
    {
        SSLSocketFactory sf = newSslSocketFactory();
        client.setSSLSocketFactory(sf);
    }

    private void initCookieStore(Context context)
    {
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        client.setCookieStore(myCookieStore);
    }
    
    private String getApiKey()
    {
        return "?key=" + API_KEY;
    }

    private SSLSocketFactory newSslSocketFactory()
    {
        try
        {
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getResources().openRawResource(R.raw.mystore);
            try
            {
                trusted.load(in, "perico".toCharArray());
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "Error opening keystore", e);
            }
            finally
            {
                in.close();
            }
            return new SSLSocketFactory(trusted);
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }

    private Map<String, Object> createMap(Object... args)
    {
        Map<String, Object> result = new HashMap<String, Object>();

        for (int i = 0; i < args.length; i += 2)
        {
            result.put((String) args[i], args[i + 1]);
        }

        return result;
    }

    private HttpEntity mapToEntity(Map<String, Object> map) throws UnsupportedEncodingException
    {
        return new StringEntity(gson.toJson(map), "UTF-8");
    }

    /*
    public void authenticate(String username, String password, final ResultCallback<Void> responseHandler)
    {
        try
        {
            Map<String, Object> data = createMap("email", username, "contrasenya", password);

            postJSON(BASE_SECURE_URL + "/loginTablet", data, new AsyncHttpResponseHandler(context, true)
            {
                @Override
                public void onSuccess(int status, String response, boolean fromCache)
                {
                    responseHandler.onSuccess(null);
                };

                @Override
                public void onFailure(Throwable error, String errorBody)
                {
                    responseHandler.onError(error, getErrorMessage(errorBody));
                }
            });
        }
        catch (Exception e)
        {
            responseHandler.onError(e, "");
        }
    }
    */

    protected String getErrorMessage(String errorBody)
    {
        try
        {
            return parseErrorBody(errorBody).getError();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    protected RestError parseErrorBody(String errorBody)
    {
        Type collectionType = new TypeToken<RestError>()
        {
        }.getType();

        return gson.fromJson(errorBody, collectionType);
    }

    protected ResponseEventos parseEventos(String json)
    {
        Type collectionType = new TypeToken<ResponseEventos>()
        {
        }.getType();

        return gson.fromJson(json, collectionType);
    }

    protected List<Butaca> parseButacas(String json)
    {
        Type collectionType = new TypeToken<List<Butaca>>()
        {
        }.getType();

        return gson.fromJson(json, collectionType);
    }

    public void getEventos(final ResultCallback<List<Evento>> responseHandler)
    {
        get(BASE_SECURE_URL + "/evento" + getApiKey(), new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(String result)
            {
                try
                {
                    ResponseEventos response = parseEventos(result);
                    responseHandler.onSuccess(response.getEventos());
                }
                catch (Exception e)
                {
                    responseHandler.onError(e, "Error recuperando eventos");    
                }
            }

            @Override
            public void onFailure(Throwable error, String errorBody)
            {
                responseHandler.onError(error, getErrorMessage(errorBody));
            }
        });
    }

    public void getButacas(int idSesion, final ResultCallback<List<Butaca>> responseHandler)
    {
        get(BASE_SECURE_URL + "/sesion/" + idSesion + "/butacas" + getApiKey(), new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(String result)
            {
                responseHandler.onSuccess(parseButacas(result));
            }

            @Override
            public void onFailure(Throwable error, String errorBody)
            {
                responseHandler.onError(error, getErrorMessage(errorBody));
            }
        });
    }

    public void updatePresentadas(int idSesion, List<Butaca> butacas, final ResultCallback<Void> responseHandler)
    {
        String url = BASE_SECURE_URL + "/sesion/" + idSesion + getApiKey();

        HttpEntity entity = null;
        try
        {
            String json = gson.toJson(butacas);
            entity = new StringEntity(json, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            responseHandler.onError(e, "Error toJson en POST");
        }

        client.post(context, url, defaultHeaders(), entity, "application/json", new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int arg0, String response)
            {
                responseHandler.onSuccess(null);
            }

            @Override
            public void onFailure(Throwable throwable, String body)
            {
                responseHandler.onError(throwable, getErrorMessage(body));
            }
        });
    }

    private void get(final String url, final AsyncHttpResponseHandler responseHandler)
    {
        client.get(context, url, defaultHeaders(), null, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int status, String result)
            {
                responseHandler.onSuccess(status, result);
            }

            @Override
            public void onFailure(Throwable throwable, String message)
            {
                responseHandler.onFailure(throwable, message);
            }
        });
    }

    private void postJSON(String url, Map<String, Object> data, final AsyncHttpResponseHandler responseHandler)
    {
        try
        {
            HttpEntity entity = mapToEntity(data);

            client.post(context, url, defaultHeaders(), entity, "application/json", responseHandler);
        }
        catch (Exception e)
        {
            responseHandler.onFailure(e, "");
        }
    }

    private void postWithFileUpload(String url, InputStream fileInputStream, String fileName, Map<String, Object> data,
            final AsyncHttpResponseHandler responseHandler)
    {
        try
        {
            RequestParams params = new RequestParams();

            if (data != null)
            {
                for (Entry<String, Object> entry : data.entrySet())
                    params.put(entry.getKey(), (String) entry.getValue());
            }

            if (fileInputStream != null)
                params.put("file", fileInputStream, fileName);

            client.post(context, url, defaultHeaders(), params, null, responseHandler);
        }
        catch (Exception e)
        {
            responseHandler.onFailure(e, "");
        }
    }

    private void delete(String url, final AsyncHttpResponseHandler responseHandler)
    {
        client.delete(context, url, defaultHeaders(), responseHandler);
    }

    private Header[] defaultHeaders()
    {
        return new Header[] { new BasicHeader("X-Requested-With", "XMLHttpRequest") };
    }

    public interface ResultCallback<T>
    {
        public void onSuccess(T successData);

        public void onError(Throwable throwable, String errorMessage);
    }

    /*
    public void saveNewAppointment(String fecha, String hora, String descripcion, int avisar,
            final ResultCallback<Void> responseHandler)
    {
        try
        {
            Map<String, Object> data = createMap("fecha", fecha, "hora", hora, "descripcion", descripcion, "avisar",
                    avisar);

            postJSON(BASE_SECURE_URL + "/citas", data, new AsyncHttpResponseHandler(context, true)
            {
                @Override
                public void onSuccess(int status, String response, boolean fromCache)
                {
                    responseHandler.onSuccess(null);
                };

                @Override
                public void onFailure(Throwable error, String errorBody)
                {
                    responseHandler.onError(error, getErrorMessage(errorBody));
                }
            });
        }
        catch (Exception e)
        {
            responseHandler.onError(e, "");
        }
    }
    */

}
