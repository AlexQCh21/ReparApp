/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });


const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

// Inicializar Firebase Admin
admin.initializeApp();

// Configuración global de las funciones
setGlobalOptions({ maxInstances: 10 });

// Función que se dispara cuando se crea un nuevo documento en la colección 'notificaciones'
exports.enviarNotificacion = onDocumentCreated("notificaciones/{notificacionId}", async (event) => {
  const snap = event.data;
  if (!snap) {
    console.error("No hay datos en el evento.");
    return;
  }

  const notiData = snap.data();
  const idUsuario = notiData.idUsuario;

  try {
    const userDoc = await admin.firestore().collection("usuarios").doc(idUsuario).get();

    if (!userDoc.exists) {
      console.error("Usuario no encontrado:", idUsuario);
      return;
    }

    const fcmToken = userDoc.data().fcmToken;

    if (!fcmToken) {
      console.error("Token FCM no disponible para el usuario:", idUsuario);
      return;
    }

    const payload = {
      token: fcmToken,
      notification: {
        title: notiData.titulo,
        body: notiData.descripcion,
      },
    };

    const response = await admin.messaging().send(payload);
    console.log("Notificación enviada con éxito:", response);
  } catch (error) {
    console.error("Error al enviar la notificación:", error);
  }
});
