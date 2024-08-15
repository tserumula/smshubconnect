# SMSHub Connect

SMSHub Connect is an SMS Gateway application for Android devices, developed in Kotlin using Android Studio. 
The app connects to a server via HTTP to periodically retrieve SMS messages in JSON format that need to be sent. 
It handles message dispatch, tracks delivery status, and provides notifications for incoming messages.

This project is a modified fork of the original [here](https://github.com/juancrescente/SMSHub/) by Juan Crescente.

## Key Changes

* SMS retrieval and processes are handled as a background service using Alarm manager.
* The http requests to the server now include health information from the android device such as Battery level and signal radio strength.
* Upgraded codebase and Android Gradle Plugin (AGP) dependency from v4.22 to v8.7
*  Removed location permission and changed the user interface on main-fragment

## why this project ?

Commercial SMS APIs are often prohibitively expensive for many use cases. As an alternative, you can use your own phone line to send SMS messages by setting up an Android phone as a gateway.

While other SMS gateway projects exist, none that I found at the time of starting this project offered a straightforward, free solution for sending and receiving SMS via an HTTP API without commercial dependencies.

## Screenshots

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/img1.png" width="auto" height="440">
<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/img2.png" width="auto" height="440">

## App release :

You can download a compiled .apk file (unsigned) from the beta release [here](https://github.com/juancrescente/SMSHub/releases/download/0.1/app-release.apk)

### App settings

you can customize the following settings directly on the application

#### Send SMS:
+ *Enable sending*: whether the app should read from the API and send messages
+ *send URL*: messages will be parsed from this URL, you return a JSON containing *message*, *number* and *id*
+ *interval*: the app will check whether there is an incoming message for sending each specific interval in minutes
+ *status URL*: once a message is sent, status will be reported to this URL via GET parameters, *id* and *status* (SENT, FAILED, DELIVERED)

#### Receive SMS:
+ *receive URL*: Message received will be posted here. If nothing is specified it will skip this action.


### How sending SMSs works

1- The application connects at regular intervals to a URL

```
POST https://yourcustomurl.com/send_api
    deviceId: 1
    action: SEND
```

2- It should read a JSON containing *message*, *number* and *id*, or an empty response if there is nothing to send
```
{ "message": "hola mundo!", "number": "3472664455", "messageId": "1" }
```

3- The app will send the SMS *message* to *number*

4- Once sent (or failed) the app will notify the status to the status URL
```
POST https://yourcustomurl.com/status_api
    deviceId: 1
    messageId: 1
    status: SENT
    action: STATUS
```

5- Once delivered the app will notify the status to the status URL

```
POST https://yourcustomurl.com/status_api
    deviceId: 1
    messageId: 1
    status: DELIVERED
    action: STATUS
```

Possible _status_ values are: SENT, FAILED, DELIVERED (notice that it is unlikely but possible to get the DELIVERED update before the SENT update due to requests delay).


### How receiving SMSs works

1- Each time a SMS is received the app will notify the received URL
```
POST https://yourcustomurl.com/received_api
    deviceId: 1
    number: 3472556699
    message: Hello man!
    action: RECEIVED
```

