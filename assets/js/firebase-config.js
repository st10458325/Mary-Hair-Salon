import { initializeApp } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-auth.js";
import { initializeFirestore, persistentLocalCache } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyAjVPCbV2ApLq-0WGINeYcujSjgOqgAa0o",
  authDomain: "mary-hair-salon.firebaseapp.com",
  projectId: "mary-hair-salon",
  storageBucket: "mary-hair-salon.firebasestorage.app",
  messagingSenderId: "355995364975",
  appId: "1:355995364975:web:fe369f7951099973b4a79a"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = initializeFirestore(app, {
  localCache: persistentLocalCache()
});