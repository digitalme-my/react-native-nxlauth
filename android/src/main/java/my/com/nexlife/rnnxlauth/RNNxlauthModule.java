
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

import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import my.com.nexlife.nxlauth.AuthManager;
import my.com.nexlife.nxlauth.AuthManager.NXLCallback;
import my.com.nexlife.nxlauth.SDKMessages;
import my.com.nexlife.nxlauth.SDKScopes;

import net.openid.appauth.AuthState;
import net.openid.appauth.TokenResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RNNxlauthModule extends ReactContextBaseJavaModule {

  private final String mTag = "RNNXLAUTHDEBUG";
  private final ReactApplicationContext reactContext;
  private AuthManager mAuthManager;
  private final String mPleaseConfigure = "Please perform authentication first";

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      Log.d(RNNxlauthModule.this.mTag, "onActivityResult function");
      if (requestCode == SDKMessages.RC_AUTH) {
        boolean status = RNNxlauthModule.this.mAuthManager.performTokenRequestSuccessful(intent);
        Log.d(RNNxlauthModule.this.mTag, "Status: " + status);
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
    Log.d(this.mTag, "Client ID: " + clientId);
    this.mAuthManager = new AuthManager.Builder(clientId, this.reactContext)
        .setTag(this.mTag)
        .setScope(SDKScopes.OPEN_ID, SDKScopes.OFFLINE)
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
  public void authorizeRequest() {
    if (this.mAuthManager == null) {
      Log.d(this.mTag, "Auth Manager is null");
      this.buildAuthConfig();
      try {
        Thread.sleep(3000);
      } catch(Exception ex) {
        Log.e(this.mTag, "Thread sleep exception: " + ex.toString());
      }
    }
    Log.d(this.mTag, "Auth Manager is not null and can proceed");

    // Discover doc not retrieved yet but already triggering startAuth
    // Reimplement build
    Intent authIntent = this.mAuthManager.startAuthentication();
    Activity currentActivity = getCurrentActivity();
    currentActivity.startActivityForResult(authIntent, SDKMessages.RC_AUTH);
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
          Log.d(RNNxlauthModule.this.mTag, "onComplete resolve");
          promise.resolve(result);
        } else {
          Log.d(RNNxlauthModule.this.mTag, "onComplete reject");
          promise.reject(ex.toString());
          ex.printStackTrace();
        }
      }
    });
  }

  @ReactMethod
  public void getAuthState(final Promise promise) {
    // if (this.mAuthManager == null) {
    //   Log.d(this.mTag, "AuthManager is null");
    //   promise.reject("Error");
    //   return;
    // }
    Log.d(this.mTag, "Retrieving authState");
    AuthState authState = this.mAuthManager.getCurrentAuthState();
    Log.d(this.mTag, "Done retrieving authState");
    
    if (authState != null && authState.getLastTokenResponse() != null) {
      WritableMap tokenResponse = this.tokenResponseToMap(authState.getLastTokenResponse());
      Log.d(this.mTag, "Authstate is not null");
      Log.d(this.mTag, tokenResponse.toString());
      promise.resolve(tokenResponse);
    } else {
      Log.d(this.mTag, "Authstate is null");
      promise.reject("Error");
    }
    // promise.resolve("WATEVA");
  }

  @ReactMethod
  public void clearAuthState() {
    if (this.mAuthManager != null) {
      this.mAuthManager.clearSharedPreferences();
    }
  }
}