
package my.com.nexlife.rnnxlauth;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableArray;

import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import my.com.nexlife.nxlauth.AuthManager;
import my.com.nexlife.nxlauth.AuthManager.NXLCallback;
import my.com.nexlife.nxlauth.SDKMessages;
import my.com.nexlife.nxlauth.SDKScopes;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationService.TokenResponseCallback;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthState;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class RNNxlauthModule extends ReactContextBaseJavaModule {

  private final String mTag = "RNNXLAUTHDEBUG";
  private final ReactApplicationContext reactContext;
  private AuthManager mAuthManager;
  private final String mPleaseConfigure = "Please perform authentication first";
  private Promise mPromise;

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      Log.d(RNNxlauthModule.this.mTag, "onActivityResult function");
      if (requestCode == SDKMessages.RC_AUTH) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException exception = AuthorizationException.fromIntent(intent);

        RNNxlauthModule.this.mAuthManager.updateAfterAuthorization(response, exception);

        if (exception != null) {
          RNNxlauthModule.this.mPromise.reject("RNAppAuth Error", "Failed to authenticate", exception);
          return;
        }

        final Promise authorizePromise = RNNxlauthModule.this.mPromise;

        AuthorizationService authService = new AuthorizationService(RNNxlauthModule.this.reactContext);
        TokenRequest tokenRequest = response.createTokenExchangeRequest();

        AuthorizationService.TokenResponseCallback tokenResponseCallback = new AuthorizationService.TokenResponseCallback() {
          @Override
          public void onTokenRequestCompleted(TokenResponse resp, AuthorizationException ex) {
            RNNxlauthModule.this.mAuthManager.updateAfterTokenResponse(resp, ex);
            if (resp != null) {
              WritableMap map = RNNxlauthModule.this.tokenResponseToMap(resp);
              authorizePromise.resolve(map);
            } else {
              authorizePromise.reject("RNAppAuth Error", "Failed exchange token", ex);
            }
          }
        };
        authService.performTokenRequest(tokenRequest, tokenResponseCallback);
      }
    }
  };

  /**
   * Public constructor
   */
  public RNNxlauthModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addActivityEventListener(mActivityEventListener);

    // Should build here
    this.buildAuthConfig();
  }

  @Override
  public String getName() {
    return "RNNxlauth";
  }

  // NXLAuth bridges
  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  private String getScopes() {
    try {
      int clientIdRes = this.reactContext.getResources().getIdentifier("nxlauth_scopes", "string", this.reactContext.getPackageName());
      String clientId = this.reactContext.getResources().getString(clientIdRes);
      return clientId;
    } catch(Exception ex) {
      return ex.toString();
    }
  }

  private String getClientId() {
    try {
      int clientIdRes = this.reactContext.getResources().getIdentifier("nxlauth_client_id", "string", this.reactContext.getPackageName());
      String clientId = this.reactContext.getResources().getString(clientIdRes);
      return clientId;
    } catch(Exception ex) {
      return ex.toString();
    }
  }

  /**
   * Function to build the auth config
   */
  private void buildAuthConfig() {
    Log.d(this.mTag, "Building Auth Config");
    String clientId = getClientId();
    String scopes = getScopes();
    Log.d(this.mTag, "Client ID: " + clientId);
    this.mAuthManager = new AuthManager.Builder(clientId, this.reactContext)
        .setTag(this.mTag)
        .setScope(scopes)
        .build();
  }

  /**
   * Function format the response
   */
  private WritableMap tokenResponseToMap(TokenResponse response) {
    WritableMap map = Arguments.createMap();

    map.putString("accessToken", response.accessToken);

    if (response.accessTokenExpirationTime != null) {
        Date expirationDate = new Date(response.accessTokenExpirationTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String expirationDateString = formatter.format(expirationDate);
        map.putString("accessTokenExpirationDate", expirationDateString);
    }

    WritableMap additionalParametersMap = Arguments.createMap();

    if (!response.additionalParameters.isEmpty()) {

        Iterator<String> iterator = response.additionalParameters.keySet().iterator();

        while(iterator.hasNext()) {
            String key = iterator.next();
            additionalParametersMap.putString(key, response.additionalParameters.get(key));
        }
    }

    map.putMap("additionalParameters", additionalParametersMap);
    map.putString("idToken", response.idToken);
    map.putString("refreshToken", response.refreshToken);
    map.putString("tokenType", response.tokenType);

    return map;
}

  @ReactMethod
  public void testFunction(String message) {
    Toast.makeText(this.reactContext, message, 5).show();
  }

  @ReactMethod
  public void authorizeRequest(Promise promise) {
    this.mPromise = promise;
    if (this.mAuthManager == null) {
      Log.d(this.mTag, "Auth Manager is null");
      this.buildAuthConfig();
      try {
        Thread.sleep(3000);
      } catch(Exception ex) {
        mPromise.reject(ex.toString());
        Log.e(this.mTag, "Thread sleep exception: " + ex.toString());
      }
    }
    Log.d(this.mTag, "Auth Manager is not null and can proceed");

    // Discover doc not retrieved yet but already triggering startAuth
    // Reimplement build
    Intent authIntent = this.mAuthManager.startAuthentication();
    Activity currentActivity = getCurrentActivity();
    if (authIntent != null) {
      currentActivity.startActivityForResult(authIntent, SDKMessages.RC_AUTH);
    }
  }

  @ReactMethod
  public void getFreshToken(final Promise promise) {
    if (this.mAuthManager == null) {
      buildAuthConfig();
      try {
        Thread.sleep(3000);
      } catch(Exception ex) {
        Log.e(this.mTag, "Thread sleep exception: " + ex.toString());
      }
    }
    this.mAuthManager.getAccessToken(new NXLCallback() {
      @Override
      public void onComplete(String result, Exception ex) {
        if (ex == null) {
          promise.resolve(result);
        } else {
          promise.reject(ex.toString());
          ex.printStackTrace();
        }
      }
    });
  }

  @ReactMethod
  public void getUserInfo(final Promise promise) {
    Log.d(this.mTag, "Get user info");
    if (this.mAuthManager == null) {
      Log.d(this.mTag, "AuthManager is null");
      buildAuthConfig();
      try {
        Thread.sleep(3000);
      } catch(Exception ex) {
        Log.e(this.mTag, "Thread sleep exception: " + ex.toString());
      }
    }
    Log.d(this.mTag, "Getting via NXLCallback");
    
    this.mAuthManager.getUserInfo(new NXLCallback() {
      @Override
      public void onComplete(String result, Exception ex) {
        Log.d(RNNxlauthModule.this.mTag, "onComplete getUserInfo");
        if (ex == null) {
          WritableMap map = Arguments.createMap();

          try {
            JSONObject obj = new JSONObject(result);
            map = jsonToWritableMap(obj);
          } catch(Exception malformedJson) {

          }

          Log.d(RNNxlauthModule.this.mTag, "onComplete resolve");
          promise.resolve(map);
        } else {
          Log.d(RNNxlauthModule.this.mTag, "onComplete reject");
          promise.reject(ex.toString());
          ex.printStackTrace();
        }
      }
    });
  }

  private WritableMap jsonToWritableMap(JSONObject jsonObject) {
    WritableMap writableMap = new WritableNativeMap();

    if (jsonObject == null) {
      return null;
    }

    Iterator<String> iterator = jsonObject.keys();
    if (!iterator.hasNext()) {
        return null;
    }

    try {
      while (iterator.hasNext()) {
        String key = iterator.next();
        Object value = jsonObject.get(key);
        if (value == null) {
          writableMap.putNull(key);
        } else if (value instanceof Boolean) {
          writableMap.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
          writableMap.putInt(key, (Integer) value);
        } else if (value instanceof Double) {
          writableMap.putDouble(key, (Double) value);
        } else if (value instanceof String) {
          writableMap.putString(key, (String) value);
        } else if (value instanceof JSONObject) {
          writableMap.putMap(key, jsonToWritableMap((JSONObject) value));
        } else if (value instanceof JSONArray) {
          writableMap.putArray(key, jsonArrayToWritableArray((JSONArray) value));
        }
      }
    } catch (JSONException ex){
            // Do nothing and fail silently
    }
    return writableMap;
  }

  private WritableArray jsonArrayToWritableArray(JSONArray jsonArray) {
    WritableArray writableArray = new WritableNativeArray();

    try {
      if (jsonArray == null) {
        return null;
      }

      if (jsonArray.length() <= 0) {
        return null;
      }

      for (int i = 0 ; i < jsonArray.length(); i++) {
        Object value = jsonArray.get(i);
        if (value == null) {
          writableArray.pushNull();
        } else if (value instanceof Boolean) {
          writableArray.pushBoolean((Boolean) value);
        } else if (value instanceof Integer) {
          writableArray.pushInt((Integer) value);
        } else if (value instanceof Double) {
          writableArray.pushDouble((Double) value);
        } else if (value instanceof String) {
          writableArray.pushString((String) value);
        } else if (value instanceof JSONObject) {
          writableArray.pushMap(jsonToWritableMap((JSONObject) value));
        } else if (value instanceof JSONArray) {
          writableArray.pushArray(jsonArrayToWritableArray((JSONArray) value));
        }
      }
    } catch (JSONException e) {
        // Do nothing and fail silently
    }

    return writableArray;
}

  @ReactMethod
  public void getAuthState(final Promise promise) {
    Log.d(this.mTag, "Retrieving authState");
    AuthState authState = this.mAuthManager.getCurrentAuthState();
    Log.d(this.mTag, "Done retrieving authState");
    
    if (authState != null && authState.getLastTokenResponse() != null) {
      WritableMap tokenResponseMap = this.tokenResponseToMap(authState.getLastTokenResponse());
      Log.d(this.mTag, "Authstate is not null");
      promise.resolve(tokenResponseMap);
    } else {
      Log.d(this.mTag, "Authstate is null");
      promise.resolve("");
    }
  }

  @ReactMethod
  public void clearAuthState() {
    if (this.mAuthManager != null) {
      this.mAuthManager.clearSharedPreferences();
      this.mAuthManager.replace(new AuthState());
    }
  }
}