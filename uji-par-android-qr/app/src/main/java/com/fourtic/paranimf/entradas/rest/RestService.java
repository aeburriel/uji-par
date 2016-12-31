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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

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
	private static final String CHARACTER_ENCODING = "UTF-8";

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
		return new StringEntity(gson.toJson(map), CHARACTER_ENCODING);
	}

	/*
	 * public void authenticate(String username, String password, final
	 * ResultCallback<Void> responseHandler) { try { Map<String, Object> data =
	 * createMap("email", username, "contrasenya", password);
	 * 
	 * postJSON(BASE_SECURE_URL + "/loginTablet", data, new
	 * AsyncHttpResponseHandler(context, true) {
	 * 
	 * @Override public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	 * responseHandler.onSuccess(null); };
	 * 
	 * @Override public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
	 * responseHandler.onError(error, getErrorMessage(errorResponse)); } }); }
	 */

	protected String getErrorMessage(byte[] errorBody) {
		try {
			String msg = new String(errorBody, CHARACTER_ENCODING);
			return parseErrorBody(msg).getError();
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

	protected List<Butaca> parseButacas(byte[] json) {
		Type collectionType = new TypeToken<List<Butaca>>() {
		}.getType();

		try {
			return gson.fromJson(new String(json, CHARACTER_ENCODING), collectionType);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public void getEventos(final ResultCallback<List<Evento>> responseHandler) {
		get(getUrl() + BASE_SECURE_URL + "/evento" + getApiKey(),
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {
						try {
							ResponseEventos responseEventos = parseEventos(new String(response, CHARACTER_ENCODING));
							responseHandler.onSuccess(responseEventos.getEventos());
						} catch (Exception e) {
							responseHandler.onError(e,
									"Error recuperando eventos");
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
						responseHandler.onError(error,
								getErrorMessage(errorResponse));
					}
				});
	}

	public void getButacas(int idSesion,
			final ResultCallback<List<Butaca>> responseHandler) {
		get(getUrl() + BASE_SECURE_URL + "/sesion/" + idSesion + "/butacas"
				+ getApiKey(), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] response) {
				responseHandler.onSuccess(parseButacas(response));
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
				responseHandler.onError(error, getErrorMessage(errorResponse));
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
			entity = new StringEntity(json, CHARACTER_ENCODING);
		} catch (Exception e) {
			responseHandler.onError(e, "Error toJson en POST");
		}

		client.post(context, url, defaultHeaders(), entity, "application/json",
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {
						responseHandler.onSuccess(null);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
						responseHandler.onError(error,
								getErrorMessage(errorResponse));
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
            entity = new StringEntity(json, CHARACTER_ENCODING);
        } catch (Exception e) {
            responseHandler.onError(e, "Error toJson en POST");
        }

        client.setTimeout(5000);
        client.post(context, url, defaultHeaders(), entity, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        client.setTimeout(30000);
                        try {
                            RestResponse restResponse = gson.fromJson(new String(response, CHARACTER_ENCODING), RestResponse.class);
                            if (restResponse.getSuccess()) {
                                responseHandler.onSuccess(null);
                            }
                            else {
                                responseHandler.onError(new EntradaPresentadaException(), context.getString(R.string.ya_presentada));
                            }
                        } catch (JsonSyntaxException e) {
                            responseHandler.onError(e, "Error fromJson al recibir la respuesta del POST");
                        } catch (UnsupportedEncodingException e) {
			        responseHandler.onError(e, "Error interpretando la respuesta del POST como UTF-8");
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
                        client.setTimeout(30000);
                        responseHandler.onError(error, getErrorMessage(errorResponse));
                    }
                });
    }

	private void get(final String url,
			final AsyncHttpResponseHandler responseHandler) {
		client.get(context, url, defaultHeaders(), null,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {
						responseHandler.onSuccess(statusCode, headers, response);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable error) {
						responseHandler.onFailure(statusCode, headers, errorResponse, error);
					}
				});
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
