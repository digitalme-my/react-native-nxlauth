/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, TouchableOpacity, Alert} from 'react-native';
import { authorizeRequest, getUserInfo, getFreshToken, getAuthState, clearAuthState, authorize, refresh, revoke, test } from 'react-native-nxlauth';


const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

const scopes = {
  scopes: ['openid', 'offline']
};

export default class App extends Component {
  constructor(props) {
    super(props)
    this.state = { count: 0,
      hasLoggedInOnce: false,
      accessToken: '',
      accessTokenExpirationDate: '',
      refreshToken: '',
      authState: '',
      userInfo: '',
      status: 'Not Authorise' }
  }

  componentWillMount() {
    this.getAuthState();

  }

  authorize = async () => {
    console.log('inside here');
    try {
      const authState = await authorize(config);
      console.log(authState);

      // this.animateState(
      //   {
      //     hasLoggedInOnce: true,
      //     accessToken: authState.accessToken,
      //     accessTokenExpirationDate: authState.accessTokenExpirationDate,
      //     refreshToken: authState.refreshToken
      //   },
      //   500
      // );
    } catch (error) {
      Alert.alert('Failed to log in', error.message);
    }
  };

  authorizeRequest = async () => {
    console.log('inside authorizeRequest');
    try {
      const authState = await authorizeRequest(scopes);
      this.setState({
        authState: authState
      })
      this.setState({
        status: "Authorised"
      })
    } catch (error) {
      console.log('Failed to log in', error.message);
    }
  };

  getAuthState = async () => {
    try {
      const currentAuthState = await getAuthState();
      console.log("Current Auth State: ", currentAuthState);
      if (currentAuthState) {
        this.setState({
          status: "Authorised",
          authState: currentAuthState
        })
      } else {
        this.setState({
          status: "Not Authorise",
          authState: currentAuthState,
          userInfo: ''
        })
      }
    } catch (error) {
      Alert.alert('Failed to getAuthState', error.message);
    }
  };

  userInfo = async () => {
    console.log('inside userInfo');
    try {
      const user = await getUserInfo();
      this.setState({
        userInfo: user.sub,
      })
      console.log(user);
    } catch (error) {
      Alert.alert('Failed to retrieve User Info', error.message);
    }
  };

  freshToken = async () => {
    console.log('inside freshToken');
    try {
      const validToken = await getFreshToken();
      console.log(validToken);
      this.getAuthState();
    } catch (error) {
      Alert.alert('Failed to get fresh token', error.message);
    }
  };


  loginPressed = () => {
    this.setState({
      count: this.state.count+1
    })
    console.log("button pressed");
    // appAuth.startAuth();
    // this.authorize();
    this.authorizeRequest();
  }

  clearStatePressed = () => {
    // this.setState({
    //   status: "Not Authorise"
    // })
    console.log("button pressed");
    // appAuth.startAuth();
    // this.authorize();
    clearAuthState();
    this.getAuthState();

  }

  freshTokenPressed = () => {
    this.freshToken();
  }

  userInfoPressed = () => {
    this.userInfo();
  }

  render() {
    return (
      <View style={styles.container}>
        <View style={{ flex: 0.7, justifyContent: 'center' }}>
        <Text>
            Status
          </Text>

          <Text style={{ borderWidth: 1, marginBottom: 10 }}>
            {this.state.status}
          </Text>
          <Text>
            access token
          </Text>

          <Text style={{ borderWidth: 1, marginBottom: 10 }}>
            {this.state.authState.accessToken}
          </Text>

           <Text>
            user info
          </Text>

          <Text style={{ borderWidth: 1, marginBottom: 10 }}>
            {this.state.userInfo}
          </Text>

        </View>
        <View style={{ flex: 0.3, paddingHorizontal: 10, justifyContent: 'center' }}>
          <TouchableOpacity
            style={styles.button}
            onPress={this.loginPressed}
          >
            <Text> Login Here </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.button}
            onPress={this.userInfoPressed}
          >
            <Text> User Info </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.button}
            onPress={this.freshTokenPressed}
          >
            <Text> Get Fresh Token </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.button}
            onPress={this.clearStatePressed}
          >
            <Text> Clear Auth State </Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-end',
    paddingHorizontal: 10
  },
  button: {
    alignItems: 'center',
    backgroundColor: '#DDDDDD',
    padding: 10
  },
  countContainer: {
    alignItems: 'center',
    padding: 10
  },
  countText: {
    color: '#FF00FF'
  }
});
