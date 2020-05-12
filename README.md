
# react-native-nxlauth

## Getting started

`$ npm install digitalme-my/react-native-nxlauth --save`

`$ yarn add digitalme-my/react-native-nxlauth`

### Mostly automatic installation

`$ react-native link react-native-nxlauth`

** extra step for ANDROID automatic installation
- Insert the following lines inside the android, **defaultConfig** block in `android/app/build.gradle`:
    ```
      defaultConfig {
        ...
        manifestPlaceholders = [
          'appAuthRedirectScheme': 'your.package.name'
        ]
      }
    ```
- Insert the following lines inside the **dependencies** block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-nxlauth')
  	```
- Insert the following lines inside main app's `android/app/src/main/res/values/strings.xml` resource file:
    ```
      <string name="nxlauth_client_id">YOUR_CLIENT_ID</string>
      <string name="nxlauth_scopes">YOUR_SCOPES</string>
      <string name="base_url">YOUR_ISSUER</string>
    
    ```


**Then follow the [Setup](#setup) steps to configure the native iOS and Android projects.**

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-react-native-nxlauth` and add `RNReactNativeNxlauth.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReactNativeNxlauth.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import my.com.nexlife.rnnxlauth.RNNxlauthPackage;` to the imports at the top of the file
  - Add `new RNReactNativeNxlauthPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-nxlauth'
  	project(':react-native-nxlauth').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-nxlauth/android')
  	```
3. Insert the following lines inside the android, **defaultConfig** block in `android/app/build.gradle`:
    ```
      defaultConfig {
        ...
        manifestPlaceholders = [
          'appAuthRedirectScheme': 'your.package.name'
        ]
      }
    ```
4. Insert the following lines inside the **dependencies** block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-nxlauth')
  	```
5. Insert the following lines inside main app's `android/app/src/main/res/values/strings.xml` resource file:
    ```
      <string name="nxlauth_client_id">YOUR_CLIENT_ID</string>
      <string name="nxlauth_scopes">YOUR_SCOPES</string>
      <string name="base_url">YOUR_ISSUER</string>
    
    ```

## Setup

### iOS Setup

To setup the iOS project, you need to perform three steps:

1. [Install native dependencies](#install-native-dependencies)
2. [Register redirect URL scheme](#register-redirect-url-scheme)
3. [Define openURL callback in AppDelegate](#define-openurl-callback-in-appdelegate)

##### Install native dependencies

This library depends on the [NXLAuth Framework](https://github.com/digitalme-my/NXLAuth-iOS) project. To
keep the React Native library agnostic of your dependency management method, the native libraries
are not distributed as part of the bridge.

1. **CocoaPods**

   With [CocoaPods](https://guides.cocoapods.org/using/getting-started.html),
   add the following line to your `Podfile`:

    `pod 'AppAuth', :git => 'https://github.com/digitalme-my/AppAuth-iOS.git'`
    
    for RN > 0.60, add below into your pod file
    
    ```
    def append_framework_search_path(target, path)
     target.build_configurations.each do |config|
      # Note that there's a space character after `$(inherited)`.
      config.build_settings["FRAMEWORK_SEARCH_PATHS"] ||= "$(inherited) "
      config.build_settings["FRAMEWORK_SEARCH_PATHS"] << path
     end
   end

   post_install do |installer|
     installer.pods_project.targets.each do |target|
       if target.name == "react-native-nxlauth"
         append_framework_search_path(target, "$(SRCROOT)/../")
       end
     end

     installer.pods_project.build_configuration_list.build_configurations.each do |configuration|  
       configuration.build_settings['CLANG_ALLOW_NON_MODULAR_INCLUDES_IN_FRAMEWORK_MODULES'] = 'YES'  
     end 
   end
    ```


   Then run 
   
    `pod install`.


2. **Download NXLAuth Framework**

   Download the NXLAuth Framework file 👉 [HERE](https://github.com/nexlife/NXLAuth-iOS/archive/master.zip) 👈
   
   - Unzip and move the NXLAuth.framework into your project.
    <img src="/images/drag_framework.gif" width="100%" height="100%" />
   
   - Add NXLAuth.framework into Embedded Binaries.
   <img src="/images/link_binary.gif" width="100%" height="100%" />
   

3. **Create a NXLAuthConfig.plist in your project.**

   Right click from your project --> New File --> Resource --> choose Property List --> named it as "NXLAuthConfig.plist"
   
   add the following line to your NXLAuthConfig.plist.

    ```
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
        <key>NXLAuthConfig</key>
        <array>
            <dict>
                <key>Issuer</key>
                <string>YOUR ISSUER</string>
                <key>ClientID</key>
                <string>YOUR CLIENT ID</string>
                <key>RedirectURI</key>
                <string>YOUR REDIRECT URL</string>
            </dict>
        </array>
    </dict>
    </plist>
    ```
    
##### Information You'll Need From Your idP

* Issuer
* Client ID
* Redirect URI

<img src="/images/configuration.gif" width="100%" height="100%" />

##### Register redirect URL scheme

If you intend to support iOS 10 and older, you need to define the supported redirect URL schemes in
your `Info.plist` as follows:

```
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLName</key>
    <string>com.your.app.identifier</string>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>io.identityserver.demo</string>
    </array>
  </dict>
</array>
```

* `CFBundleURLName` is any globally unique string. A common practice is to use your app identifier.
* `CFBundleURLSchemes` is an array of URL schemes your app needs to handle. The scheme is the
  beginning of your OAuth Redirect URL, up to the scheme separator (`:`) character.

##### Define openURL callback in AppDelegate

You need to retain the auth session, in order to continue the
authorization flow from the redirect. Follow these steps:

`RNNxlauth` will call on the given app's delegate via `[UIApplication sharedApplication].delegate`.
Furthermore, `RNNxlauth` expects the delegate instance to conform to the protocol `RNNxlauthAuthorizationFlowManager`.
Make `AppDelegate` conform to `RNNxlauthAuthorizationFlowManager` with the following changes to `AppDelegate.h`:

```diff
+ #import "RNNxlauthAuthorizationFlowManager.h"

- @interface AppDelegate : UIResponder <UIApplicationDelegate>
+ @interface AppDelegate : UIResponder <UIApplicationDelegate, RNNxlauthAuthorizationFlowManager>

+ @property(nonatomic, weak)id<RNNxlauthAuthorizationFlowManagerDelegate>authorizationFlowManagerDelegate;
```

The authorization response URL is returned to the app via the iOS openURL app delegate method, so
you need to pipe this through to the current authorization session (created in the previous
instruction). Thus, implement the following method from `UIApplicationDelegate` in `AppDelegate.m`:

```swift
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *, id> *)options {
 return [self.authorizationFlowManagerDelegate resumeExternalUserAgentFlowWithURL:url];
}
```
## Remember
The universal framework will run on both simulators and Devices. But still there is a problem.

Need to remove unused architectures before submit to Apple app store. Because Apple doesn’t allow the application with unused architectures to the App Store.
Please make sure that you have Remove Unused Architectures Script added in your project while releasing your app to app store.

##### Remove Unused Architectures
Select the Project, Choose Target → Project Name → Select Build Phases → Press “+” → New Run Script Phase → Name the Script as “Remove Unused Architectures Script”.

```
if [ "${CONFIGURATION}" = "Release" ]; then
FRAMEWORK="NXLAuth"
FRAMEWORK_EXECUTABLE_PATH="${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}/$FRAMEWORK.framework/$FRAMEWORK"
EXTRACTED_ARCHS=()
for ARCH in $ARCHS
do
lipo -extract "$ARCH" "$FRAMEWORK_EXECUTABLE_PATH" -o "$FRAMEWORK_EXECUTABLE_PATH-$ARCH"
EXTRACTED_ARCHS+=("$FRAMEWORK_EXECUTABLE_PATH-$ARCH")
done
lipo -o "$FRAMEWORK_EXECUTABLE_PATH-merged" -create "${EXTRACTED_ARCHS[@]}"
rm "${EXTRACTED_ARCHS[@]}"
rm "$FRAMEWORK_EXECUTABLE_PATH"
mv "$FRAMEWORK_EXECUTABLE_PATH-merged" "$FRAMEWORK_EXECUTABLE_PATH"
fi
```

This run script removes the unused Simulator architectures only while pushing the Application to the App Store.


## Example App
 [CLICK HERE](https://github.com/nexlife/react-native-nxlauth/tree/master/Example)


## Usage
```javascript
import RNReactNativeNxlauth from 'react-native-react-native-nxlauth';

// TODO: What to do with the module?
RNReactNativeNxlauth;
```
  
