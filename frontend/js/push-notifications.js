window.subscribeToPush = async function(publicKey, vaadinElement) {
    if (!('serviceWorker' in navigator)) {
        console.warn("Service workers are not supported.");
        return;
    }
    if (!('PushManager' in window)) {
        console.warn("Push API is not supported.");
        return;
    }

    let swRegistration = null;
    try {
        swRegistration = await navigator.serviceWorker.register('/sw.js');
        console.log('Service Worker registered!');
    } catch (error) {
        console.error('Service Worker registration failed', error);
        return;
    }

    let subscription = null;
    try {
        subscription = await swRegistration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: publicKey
        });
        console.log('Push subscription successful: ', subscription);
    } catch (error) {
        console.error('Push subscription failed: ', error);
        return;
    }

    try {
        // Kličemo strežniško metodo preko reference na Vaadin element
        const subscriptionResult = await vaadinElement.$server.requestPushSubscription(subscription);
        return subscriptionResult;
    } catch (error) {
        console.error('Push subscription failed:', error);
        throw error;
    }
};


self.addEventListener('push', (event) => {
    try {
        const payload = event.data.json();
        const title = payload.title;
        const options = {
            body: payload.body, // Ensure this matches the JSON key from the server
            icon: '/icons/icon-192x192.png',
            badge: '/icons/badge-72x72.png'
        };
        event.waitUntil(self.registration.showNotification(title, options));
    } catch (error) {
        console.error('Error processing push event:', error);
    }
});