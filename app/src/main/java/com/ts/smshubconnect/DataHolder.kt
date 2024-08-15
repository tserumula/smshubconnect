package com.ts.smshubconnect

const val ALARM_REQUEST_CODE = 130
const val ALARM_INTERVAL_MILLIS = 600000L //10 min
const val WAKELOCK_MILLIS = 5*60*1000L /*5 minutes*/

const val SENT_SMS_FLAG = "SMS_SENT"
const val RECEIVED_SMS_FLAG = "SMS_RECEIVED"
const val DELIVER_SMS_FLAG = "DELIVER_SMS"

const val APP_STATUS_SMS_KEY = "sms"
const val APP_STATUS_KEY = "status"
const val APP_SERVER_HOST_KEY = "host"
const val APP_CONNECTION_KEY = "connect"
const val APP_LAST_CONNECT_KEY = "last"

const val MY_PERMISSIONS_REQUEST_SEND_SMS = 1
const val MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10

const val APP_TOKEN_KEY = "ABCD123" //Put your app identifier token here (if needed)
