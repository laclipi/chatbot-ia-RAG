# Chatbot IA

API REST para gestionar negocios, su catálogo, las reservas y un chat público por negocio. El aislamiento es por pertenencia: un usuario autenticado solo lee y modifica los negocios a los que está vinculado.

## Requisitos

- Java 21
- Docker, para PostgreSQL
- Maven Wrapper, incluido en `backend` (`mvnw.cmd` en Windows)

## Configuración

La aplicación lee la clave de firma JWT desde el entorno. Tiene que tener al menos 32 caracteres. Este valor es local y no debe subirse a Git:

```powershell
$env:JWT_SECRET = "cambia-esta-clave-local-por-otra-de-32-caracteres"
```

En cmd:

```bat
set JWT_SECRET=cambia-esta-clave-local-por-otra-de-32-caracteres
```

PostgreSQL local está descrito en `docker-compose.yml`. `backend/src/main/resources/application.properties` apunta a `localhost:5433` y a la base `chatbot_ia`. El usuario y la contraseña de esa base son los del Compose; no hace falta copiarlos aquí.

## Arranque

Desde la raíz del repositorio:

```powershell
docker compose up -d
```

Con `JWT_SECRET` definido en la misma terminal, desde `backend`:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`.

## Pruebas

Las pruebas de contexto arrancan la aplicación y necesitan PostgreSQL en marcha y `JWT_SECRET` definido. Desde `backend`:

```powershell
.\mvnw.cmd test
```

## Autenticación y aislamiento

`POST /api/auth/register` y `POST /api/auth/login` son públicos. El login correcto devuelve `accessToken` y `tokenType` con valor `Bearer`. El resto de rutas administrativas exigen esa cabecera:

```http
Authorization: Bearer <accessToken>
```

El token dura 30 minutos. Las credenciales incorrectas y las cuentas inexistentes responden HTTP 401 con el mismo cuerpo de error, sin indicar cuál de las dos ha fallado.

Crear un negocio vincula al usuario autenticado mediante una pertenencia. A partir de ahí, negocios, servicios y reservas comprueban esa pertenencia. Un token válido de otro usuario recibe HTTP 403.

El chat es la excepción pública: `POST /api/businesses/{businessId}/chat` no lleva token. Solo responde con los servicios activos de un negocio activo.

## Endpoints principales

| Método | Ruta | Acceso |
| --- | --- | --- |
| POST | `/api/auth/register` | Público |
| POST | `/api/auth/login` | Público |
| GET | `/api/health` | Token |
| GET, POST | `/api/businesses` | Token; el listado solo incluye los negocios del usuario |
| GET | `/api/businesses/{id}` | Token y pertenencia |
| GET, POST | `/api/businesses/{businessId}/services` | Token y pertenencia |
| GET, PUT, DELETE | `/api/businesses/{businessId}/services/{serviceId}` | Token y pertenencia |
| PATCH | `/api/businesses/{businessId}/services/{serviceId}/active?active=true` | Token y pertenencia |
| GET | `/api/businesses/{businessId}/services/active` | Token y pertenencia |
| GET, POST | `/api/businesses/{businessId}/appointments` | Token y pertenencia |
| PATCH | `/api/businesses/{businessId}/appointments/{appointmentId}/confirm` | Token y pertenencia |
| PATCH | `/api/businesses/{businessId}/appointments/{appointmentId}/cancel` | Token y pertenencia |
| POST | `/api/businesses/{businessId}/chat` | Público |

Los errores controlados usan `ApiError`: `timestamp`, `status`, `error`, `message` y `path`.

## Colección Postman

La colección está en `postman/collections/Chatbot IA API`. También hay un entorno en `postman/environments/Local.environment.yaml` con `baseUrl`.

Ábrela con Postman o con la extensión de Postman en el editor. La variable `baseUrl` de la colección vale `http://localhost:8080`. El login guarda `accessToken` y las peticiones administrativas heredan ese Bearer. La carpeta Chat tiene autenticación desactivada.

Orden útil de la colección:

1. Auth: Register User y después Login User. Las dos usan `userEmail` y `userPassword` de la colección. Repetir el registro responde 409 y el login sigue siendo válido.
2. Health, ya con el token heredado.
3. Businesses: Create business guarda `businessId`.
4. Offered Services: Create service usa ese `businessId` y guarda `offeredServiceId`.
5. Chat, sin token, sobre `businessId`.
6. Appointments: Create Appointment usa esos identificadores, calcula una fecha futura y guarda `appointmentId`. Confirm Appointment y después Cancel Appointment operan sobre esa reserva.

El resto de peticiones de la colección se mantienen para consultar, validar y probar casos de error. Las que apuntan a un negocio fijo siguen usando el identificador escrito en cada petición.
