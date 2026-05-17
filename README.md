# AppMoviles — Distribuidora de Alimentos con Despacho a Domicilio

## Descripción del Proyecto

Aplicación móvil Android desarrollada para una empresa de distribución de alimentos, que permite a los clientes realizar compras en línea con cálculo automático del costo de despacho según las reglas del negocio. Incluye autenticación SSO con Google, registro de ubicación GPS al iniciar sesión, catálogo de productos, carrito de compras y monitoreo de temperatura del camión de reparto en tiempo real con sistema de alarma.

Además, se integró Google Maps SDK para visualizar la ubicación actual del dispositivo móvil y calcular la distancia aproximada respecto a un punto base definido para el sistema de despacho.

---

# Acceso de Prueba

Correo: admin@appsmoviles.cl

Contraseña: Admin1234

La aplicación también permite autenticación mediante cuenta Google utilizando Firebase Authentication.

---
# Caso de Negocio

La distribuidora ofrece servicio de despacho a domicilio con las siguientes reglas:

| Total de Compra   | Costo de Despacho                  |
| ----------------- | ---------------------------------- |
| $50.000 o más     | Gratis dentro de un radio de 20 km |
| $25.000 – $49.999 | $150 por kilómetro                 |
| Menos de $25.000  | $300 por kilómetro                 |

Adicionalmente, los productos que requieren cadena de frío (carnes y mariscos congelados) son monitoreados mediante la temperatura del camión de reparto. Si la temperatura supera el límite de -18°C, se emite una alarma automática en el dispositivo móvil.

---

# Decisión Técnica — Compatibilidad Android

El enunciado del proyecto menciona que el administrador utiliza Android Lollipop (API 21), mientras que la mayoría de los clientes utilizan Android Oreo (API 26).

Se tomó la decisión de establecer la versión mínima en API 26 (Android Oreo) por las siguientes razones técnicas:

* Android Lollipop (API 21) fue lanzado en 2014 y representa menos del 1% de los dispositivos activos actualmente.
* Las dependencias utilizadas en este proyecto, especialmente Firebase Authentication, Google Sign-In y FusedLocationProvider, tienen comportamientos inconsistentes y limitados en API 21.
* El canal de notificaciones utilizado para la alarma de temperatura fue introducido en API 26 como requisito obligatorio.
* El segmento principal de usuarios utiliza Android Oreo o superior.

Conclusión: No incluir soporte para API 21 no es un error de implementación sino una decisión técnica justificada orientada a garantizar estabilidad y compatibilidad.

---

# Tecnologías Utilizadas

| Tecnología                 | Uso                                                  |
| -------------------------- | ---------------------------------------------------- |
| Android Studio             | Entorno de desarrollo                                |
| Kotlin                     | Lenguaje de programación                             |
| Firebase Authentication    | Login con email/contraseña y Google SSO              |
| Firebase Realtime Database | Almacenamiento GPS, productos, pedidos y temperatura |
| Google Play Services       | Geolocalización y autenticación Google               |
| Google Maps SDK            | Visualización GPS y cálculo aproximado de distancia  |
| RecyclerView               | Lista de productos                                   |
| NotificationManager        | Alarmas de temperatura                               |
| GitHub Projects            | Gestión de tareas e historias de usuario             |

---

# Compatibilidad

| Usuario       | Versión Android      | API Level | Soporte                 |
| ------------- | -------------------- | --------- | ----------------------- |
| Clientes      | Android 8.0 Oreo     | API 26    | ✅ Soportado             |
| Administrador | Android 5.0 Lollipop | API 21    | ⚠️ Ver decisión técnica |

---

# Funcionalidades Implementadas

*Login con correo y contraseña mediante Firebase Authentication
*Login con cuenta Google (SSO)
*Registro automático de posición GPS al iniciar sesión
*Catálogo de productos desde Firebase Realtime Database
*Carrito de compras
*Cálculo automático del costo de despacho
*Confirmación y registro de pedidos
*Monitoreo de temperatura en tiempo real
*Alarma automática cuando la temperatura supera -18°C
*Visualización de ubicación mediante Google Maps
*Cálculo aproximado de distancia entre cliente y local base
*Integración GPS en tiempo real para simulación de despacho
*Mapa integrado en el resumen del pedido
*Visualización automática de ruta aproximada entre cliente y local principal
*Visualización automática de distancia en kilómetros

---

# Arquitectura del Proyecto

```txt
AppMoviles/
├── app/
│ ├── src/main/
│ │ ├── java/com/tuapp/appsmoviles/
│ │ │ ├── AppMoviles.kt
│ │ │ ├── LoginActivity.kt
│ │ │ ├── MenuActivity.kt
│ │ │ ├── CatalogoActivity.kt
│ │ │ ├── CarritoActivity.kt
│ │ │ ├── TemperaturaActivity.kt
│ │ │ ├── MapaActivity.kt
│ │ │ ├── Producto.kt
│ │ │ ├── ProductoAdapter.kt
│ │ │ └── utils/
│ │ │ ├── DespachoCalculator.kt
│ │ │ └── GpsHelper.kt
│ │ ├── res/
│ │ │ ├── layout/
│ │ │ │ ├── activity_login.xml
│ │ │ │ ├── activity_menu.xml
│ │ │ │ ├── activity_catalogo.xml
│ │ │ │ ├── activity_carrito.xml
│ │ │ │ ├── activity_temperatura.xml
│ │ │ │ ├── activity_mapa.xml
│ │ │ │ └── item_producto.xml
│ │ └── AndroidManifest.xml
│ └── google-services.json
├── README.md
└── .gitignore
```

---

# Estructura Firebase Realtime Database
```txt
{
  "usuarios": {
    "{uid}": {
      "gps": {
        "latitud": -33.4489,
        "longitud": -70.6693,
        "timestamp": 1700000000000
      }
    }
  },
  "productos": {
    "p001": {
      "nombre": "Pechuga de Pollo 1kg",
      "precio": 4990,
      "categoria": "carnes",
      "requiere_frio": true
    }
  },
  "pedidos": {
    "{uid}": {
      "{pedidoId}": {
        "total_compra": 35000,
        "costo_despacho": 1200,
        "total_final": 36200,
        "regla_aplicada": "Compra entre $25.000 y $49.999 — Tarifa: $150/km",
        "fecha": 1700000000000
      }
    }
  },
  "temperatura": {
    "camion1": {
      "valor": -20,
      "timestamp": 1700000000000
    }
  }
}
```
# Reglas de Seguridad Firebase
```txt
{
  "rules": {
    "productos": {
      ".read": "auth != null",
      ".write": false
    },
    "usuarios": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "pedidos": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "temperatura": {
      ".read": "auth != null",
      ".write": false
    }
  }
}
```

# Instalación y Configuración

1. Clonar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Agregar el archivo `google-services.json` en la carpeta `/app`.
4. Agregar API KEY de Google Maps en `AndroidManifest.xml`.
5. Sincronizar Gradle.
6. Ejecutar en emulador o dispositivo físico con mínimo API 26.

---

# Simulación de Datos en Tiempo Real

Como no se dispone de sensores físicos, la temperatura del camión se simula directamente desde Firebase Console:

1. Ir a Firebase Console → Realtime Database.
2. Navegar a `temperatura → camion1 → valor`.
3. Editar el valor manualmente.
4. La app refleja el cambio en tiempo real.
5. Si el valor supera -18°C se dispara la alarma automáticamente.

---

# Integración GPS y Google Maps

La aplicación utiliza Google Maps SDK junto con los servicios de localización de Android para visualizar la ubicación actual del dispositivo móvil.

Se definió un punto base correspondiente al local principal de despacho y posteriormente se calcula la distancia aproximada entre el cliente y el local utilizando coordenadas GPS reales.

La distancia obtenida puede utilizarse como referencia para calcular el valor del despacho según las reglas de negocio definidas en el sistema.

Además, el resumen final del pedido incorpora un mapa integrado que permite visualizar la ubicación actual del cliente, el local principal y la ruta aproximada entre ambos puntos utilizando ajuste automático de cámara.

---

# Plan de Pruebas

# Plan de Pruebas

| N° | Caso de prueba | Entrada | Resultado esperado | Estado |
| -- | -------------- | -------- | ------------------ | ------- |
| 1 | Login con correo y contraseña | Usuario y contraseña válidos | Acceso correcto al menú principal | PASS |
| 2 | Login mediante cuenta Google | Cuenta Gmail válida | Inicio de sesión exitoso | PASS |
| 3 | Visualización del catálogo | Usuario autenticado | Productos visibles correctamente | PASS |
| 4 | Agregar productos al carrito | Selección de productos | Actualización correcta del total | PASS |
| 5 | Obtención de ubicación GPS | GPS habilitado | Lectura correcta de ubicación actual | PASS |
| 6 | Visualización Google Maps | Acceso al módulo mapa | Mapa cargado correctamente | PASS |
| 7 | Cálculo automático de distancia | Ubicación GPS obtenida | Distancia calculada respecto al local | PASS |
| 8 | Cálculo de despacho | Compra y distancia válidas | Valor calculado según reglas del negocio | PASS |
| 9 | Monitoreo de temperatura | Valor temperatura normal | Actualización correcta en tiempo real | PASS |
| 10 | Alarma de temperatura | Temperatura superior a -18°C | Notificación de alerta en pantalla | PASS |
| 11 | Visualización mapa integrado en pedido | Distancia calculada correctamente | Mapa mostrado con ubicación cliente y local | PASS |
| 12 | Registro de pedido | Pedido confirmado | Pedido almacenado correctamente en Firebase | PASS |

---

# Capturas de Pantalla

<img width="956" height="1085" alt="Captura de pantalla 2026-05-15 164914" src="https://github.com/user-attachments/assets/c2462d8d-070d-4ac3-a912-82b22e0634f4" />
<img width="1339" height="786" alt="Captura de pantalla 2026-05-16 211404" src="https://github.com/user-attachments/assets/ef1b40d2-afca-42fb-8297-d69f38839855" />


---

# Gestión del Proyecto

Las historias de usuario, tareas y avances se gestionan mediante [GitHub Projects.](https://github.com/santini84cl/AppMoviles/projects).

---

# Historias de Usuario

| ID    | Historia                          | Estado       |
| ----- | --------------------------------- | ------------ |
| HU-01 | Login con email y contraseña      | ✅ Completado |
| HU-02 | Login con Google SSO              | ✅ Completado |
| HU-03 | Registro GPS al iniciar sesión    | ✅ Completado |
| HU-04 | Catálogo de productos             | ✅ Completado |
| HU-05 | Cálculo automático de despacho    | ✅ Completado |
| HU-06 | Confirmación y registro de pedido | ✅ Completado |
| HU-07 | Monitor de temperatura con alarma | ✅ Completado |
| HU-08 | Integración Google Maps y GPS     | ✅ Completado |
| HU-09 | Documentación GitHub              | ✅ Completado |

---

# Equipo de Desarrollo

| Integrante      | Rol                     |
| --------------- | ----------------------- |
| Cristian Santos | Desarrollador principal |
| Luciano Quezada | Desarrollador principal |

---

# Licencia
Proyecto educativo realizado por estudiantes de cuarto semestre de la carrera de programacion y analisis de sistemas AIEP 2026
