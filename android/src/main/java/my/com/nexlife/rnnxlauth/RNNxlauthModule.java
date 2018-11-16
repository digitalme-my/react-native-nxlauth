
package my.com.nexlife.rnnxlauth;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import my.com.nexlife.nxlauth.AuthManager;
import my.com.nexlife.nxlauth.AuthManager.NXLCallback;
import my.com.nexlife.nxlauth.SDKMessages;
import my.com.nexlife.nxlauth.SDKScopes;

public class RNNxlauthModule extends ReactContextBaseJavaModule {

  private final String mTag = "RNNXLAUTH";
  private final ReactApplicationContext reactContext;
  private AuthManager mAuthManager;
  // private boolean mAuthenticated;
  private final String mPleaseConfigure = "Please perform authentication first";

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == SDKMessages.RC_AUTH) {
        boolean status = RNNxlauthModule.this.mAuthManager.performTokenRequestSuccessful(intent);
        Log.d(RNNxlauthModule.this.mTag, "Status: " + status);
        // RNNxlauthModule.this.mAuthenticated = status;
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
  }

  @Override
  public String getName() {
    return "RNNxlauthModule";
  }

  // NXLAuth bridges
  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  @ReactMethod
  public void buildAuthConfig(String clientId) {
    this.mAuthManager = new AuthManager.Builder(clientId, this.reactContext).setTag(this.mTag)
        .setScope(SDKScopes.OPEN_ID, SDKScopes.OFFLINE).build();
  }

  @ReactMethod
  public void startAuthentication(final Callback callback) {
    Intent authIntent = this.mAuthManager.startAuthentication();
    Activity currentActivity = getCurrentActivity();
    currentActivity.startActivityForResult(authIntent, SDKMessages.RC_AUTH);
  }

  @ReactMethod
  public void getAccessToken(final Callback callback) {
    this.mAuthManager.getAccessToken(new NXLCallback() {
      @Override
      public void onComplete(String result, Exception ex) {
        if (ex == null) {
          callback.invoke(result);
        } else {
          callback.invoke(ex.toString());
        }
      }
    });
  }

  @ReactMethod
  public void getUserInfo(final Callback callback) {
    this.mAuthManager.getUserInfo(new NXLCallback() {
      @Override
      public void onComplete(String result, Exception ex) {
        if (ex == null) {
          callback.invoke(result);
        } else {
          callback.invoke(ex.toString());
        }
      }
    });
  }
}