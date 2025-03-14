# Firebase Cloud Messaging (FCM) Setup Instructions

Firebase Cloud Messaging (FCM) is a cross-platform messaging solution that allows you to reliably send push notifications to Android, iOS, and web applications. This document provides step-by-step instructions to configure FCM for your project and integrate it with your Vaadin application.

---

## Prerequisites

- **Firebase Account:** Create or use an existing Firebase project in the [Firebase Console](https://console.firebase.google.com/).
- **Project Configuration:** Your application should be set up as a Progressive Web App (PWA) if you plan to use web push notifications.
- **HTTPS Requirement:** Push notifications and Service Workers require your application to be served over HTTPS.

---

## 1. Create/Select a Firebase Project

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Click on **"Add project"** to create a new project or select an existing project.
3. Follow the on-screen steps to configure your project.

---

## 2. Enable Cloud Messaging

1. In the Firebase Console, navigate to **"Cloud Messaging"** from the left sidebar.
2. Verify that Cloud Messaging is enabled for your project.

---

## 3. Obtain Server Key and Sender ID

1. In the **"Cloud Messaging"** section, locate the **Server key** and **Sender ID**.
2. **Copy the Server key**—this will be used as the `MOBILE_PUSH_API_KEY` in your application's configuration.
3. Optionally, note the **Sender ID** for client configuration.

---

## 4. Configure Your Application

### Add FCM Properties

In your `application.properties` (or equivalent configuration file), add:

```properties
mobile.push.api.url=https://fcm.googleapis.com/fcm/send
mobile.push.api.key=YOUR_FCM_SERVER_KEY_HERE
