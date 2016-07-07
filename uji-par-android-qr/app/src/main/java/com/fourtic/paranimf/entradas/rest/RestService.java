package com.fourtic.paranimf.entradas.rest;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.SettingsActivity;
import com.fourtic.paranimf.entradas.data.Butaca;
import com.fourtic.paranimf.entradas.data.Evento;
import com.fourtic.paranimf.entradas.data.ResponseEventos;
import com.fourtic.paranimf.entradas.exception.EntradaPresentadaException;
import com.fourtic.paranimf.entradas.socket.MySSLSocketFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Singleton
public class RestService {
	private static final String PORT_SEPARATOR = ":";

	private static String BASE_SECURE_URL = "/par-public/rest";

	private AsyncHttpClient client;
	private Gson gson;
	private Context context;
	private String url;
	private String apiKey;
	private boolean extScan;

	@Inject
	public RestService(Application application) {
		setURLFromPreferences(application);
		setAPIKeyFromPreferences(application);
		setLectorExternoFromPreferences(application);
		this.context = application;
		this.client = new AsyncHttpClient();
		this.gson = new Gson();

		client.setTimeout(30000);
		initCookieStore(context);
		initSsl();
	}

	public void setURLFromPreferences(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String host = sharedPref.getString(SettingsActivity.PREF_HOST, "").trim();
		String port = sharedPref.getString(SettingsActivity.PREF_PORT, "").trim();

		if (host.endsWith("/"))
			host = host.substring(0, host.length() - 1);

		if (port != null && port.length() > 0)
			host += PORT_SEPARATOR + port;

		this.url = host;
	}

	public void setAPIKeyFromPreferences(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		this.apiKey = sharedPref.getString(SettingsActivity.PREF_APIKEY, "").trim();
	}

	public void setLectorExternoFromPreferences(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		this.extScan = sharedPref.getBoolean(SettingsActivity.PREF_EXT_SCAN, false);
	}

	private void initSsl() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);
			MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			client.setSSLSocketFactory(sf);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private void initCookieStore(Context context) {
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);
	}

	private String getUrl() {
		return this.url;
	}

	private String getApiKey() {
		return "?key=" + this.apiKey;
	}

	public boolean hasExtScan() {
		return this.extScan;
	}

	private Map<String, Object> createMap(Object... args) {
		Map<String, Object> result = new HashMap<String, Object>();

		for (int i = 0; i < args.length; i += 2) {
			result.put((String) args[i], args[i + 1]);
		}

		return result;
	}

	private HttpEntity mapToEntity(Map<String, Object> map)
			throws UnsupportedEncodingException {
		return new StringEntity(gson.toJson(map), "UTF-8");
	}

	/*
	 * public void authenticate(String username, String password, final
	 * ResultCallback<Void> responseHandler) { try { Map<String, Object> data =
	 * createMap("email", username, "contrasenya", password);
	 * 
	 * postJSON(BASE_SECURE_URL + "/loginTablet", data, new
	 * AsyncHttpResponseHandler(context, true) {
	 * 
	 * @Override public void onSuccess(int status, String response, boolean
	 * fromCache) { responseHandler.onSuccess(null); };
	 * 
	 * @Override public void onFailure(Throwable error, String errorBody) {
	 * responseHandler.onError(error, getErrorMessage(errorBody)); } }); } catch
	 * (Exception e) { responseHandler.onError(e, ""); } }
	 */

	protected String getErrorMessage(String errorBody) {
		try {
			return parseErrorBody(errorBody).getError();
		} catch (Exception e) {
			return "";
		}
	}

	protected RestError parseErrorBody(String errorBody) {
		Type collectionType = new TypeToken<RestError>() {
		}.getType();

		return gson.fromJson(errorBody, collectionType);
	}

	protected ResponseEventos parseEventos(String json) {
		Type collectionType = new TypeToken<ResponseEventos>() {
		}.getType();

		return gson.fromJson(json, collectionType);
	}

	protected List<Butaca> parseButacas(String json) {
		Type collectionType = new TypeToken<List<Butaca>>() {
		}.getType();

		return gson.fromJson(json, collectionType);
	}

	public void getEventos(final ResultCallback<List<Evento>> responseHandler) {
		get(getUrl() + BASE_SECURE_URL + "/evento" + getApiKey(),
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String result) {
						try {
							ResponseEventos response = parseEventos(result);
							responseHandler.onSuccess(response.getEventos());
						} catch (Exception e) {
							responseHandler.onError(e,
									"Error recuperando eventos");
						}
					}

					@Override
					public void onFailure(Throwable error, String errorBody) {
						responseHandler.onError(error,
								getErrorMessage(errorBody));
					}
				});
	}

	public void getButacas(int idSesion,
			final ResultCallback<List<Butaca>> responseHandler) {
		get(getUrl() + BASE_SECURE_URL + "/sesion/" + idSesion + "/butacas"
				+ getApiKey(), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String result) {
				responseHandler.onSuccess(parseButacas(result));
			}

			@Override
			public void onFailure(Throwable error, String errorBody) {
				responseHandler.onError(error, getErrorMessage(errorBody));
			}
		});
	}

	public void updatePresentadas(int idSesion, List<Butaca> butacas,
			final ResultCallback<Void> responseHandler) {
		String url = getUrl() + BASE_SECURE_URL + "/sesion/" + idSesion
				+ getApiKey();

		HttpEntity entity = null;
		try {
			String json = gson.toJson(butacas);
			entity = new StringEntity(json, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			responseHandler.onError(e, "Error toJson en POST");
		}

		client.post(context, url, defaultHeaders(), entity, "application/json",
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int arg0, String response) {
						responseHandler.onSuccess(null);
					}

					@Override
					public void onFailure(Throwable throwable, String body) {
						responseHandler.onError(throwable,
								getErrorMessage(body));
					}
				});
	}

    public void updateOnlinePresentada(int idSesion, Butaca butaca,
                                  final ResultCallback<Void> responseHandler) {
        String url = getUrl() + BASE_SECURE_URL + "/sesion/" + idSesion
                + "/online" +  getApiKey();

        HttpEntity entity = null;
        try {
            String json = gson.toJson(butaca);
            entity = new StringEntity(json, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            responseHandler.onError(e, "Error toJson en POST");
        }

        client.setTimeout(5000);
        client.post(context, url, defaultHeaders(), entity, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int arg0, String response) {
                        client.setTimeout(30000);
                        try {
                            RestResponse restResponse = gson.fromJson(response, RestResponse.class);
                            if (restResponse.getSuccess()) {
                                responseHandler.onSuccess(null);
                            }
                            else {
                                responseHandler.onError(new EntradaPresentadaException(), context.getString(R.string.ya_presentada));
                            }
                        } catch (JsonSyntaxException e) {
                            responseHandler.onError(e, "Error fromJson al recibir la respuesta del POST");
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable, String body) {
                        client.setTimeout(30000);
                        responseHandler.onError(throwable, getErrorMessage(body));
                    }
                });
    }

	private void get(final String url,
			final AsyncHttpResponseHandler responseHandler) {
		client.get(context, url, defaultHeaders(), null,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int status, String result) {
						responseHandler.onSuccess(status, result);
					}

					@Override
					public void onFailure(Throwable throwable, String message) {
						responseHandler.onFailure(throwable, message);
					}
				});
	}

	private void postJSON(String url, Map<String, Object> data,
			final AsyncHttpResponseHandler responseHandler) {
		try {
			HttpEntity entity = mapToEntity(data);

			client.post(context, url, defaultHeaders(), entity,
					"application/json", responseHandler);
		} catch (Exception e) {
			responseHandler.onFailure(e, "");
		}
	}

	private void postWithFileUpload(String url, InputStream fileInputStream,
			String fileName, Map<String, Object> data,
			final AsyncHttpResponseHandler responseHandler) {
		try {
			RequestParams params = new RequestParams();

			if (data != null) {
				for (Entry<String, Object> entry : data.entrySet())
					params.put(entry.getKey(), (String) entry.getValue());
			}

			if (fileInputStream != null)
				params.put("file", fileInputStream, fileName);

			client.post(context, url, defaultHeaders(), params, null,
					responseHandler);
		} catch (Exception e) {
			responseHandler.onFailure(e, "");
		}
	}

	private void delete(String url,
			final AsyncHttpResponseHandler responseHandler) {
		client.delete(context, url, defaultHeaders(), responseHandler);
	}

	private Header[] defaultHeaders() {
		return new Header[] { new BasicHeader("X-Requested-With",
				"XMLHttpRequest") };
	}

	public interface ResultCallback<T> {
		public void onSuccess(T successData);

		public void onError(Throwable throwable, String errorMessage);
	}
}
