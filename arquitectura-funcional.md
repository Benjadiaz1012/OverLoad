# Overload - Documento Maestro de Arquitectura Funcional

## 1. Planteamiento del problema
El seguimiento del progreso en el entrenamiento para hipertrofia de alta intensidad presenta una dificultad significativa cuando se intenta gestionar mediante métodos convencionales como notas físicas o aplicaciones de texto genéricas. Un practicante experimentado requiere monitorear variables críticas como el RIR (Repeticiones en Reserva), el cual indica la proximidad al fallo muscular en cada serie. Esta falta de orden dificulta la toma de decisiones informadas sobre cuándo aumentar las cargas o ajustar el volumen de trabajo.

A esto se suma que la dependencia de una conexión constante a internet puede convertirse en una fuente de interrupción durante la sesión. Las fluctuaciones en la red suelen generar tiempos de carga que rompen el ritmo del entrenamiento y desincentivan el registro inmediato de los datos, lo que compromete la precisión del seguimiento a largo plazo.

Para el usuario que se inicia, el obstáculo principal es el desconocimiento técnico. Sin una guía clara sobre qué ejercicios realizar o cómo ejecutarlos correctamente, el principiante suele caer en desequilibrios biomecánicos o en una selección aleatoria de movimientos. 

Ambas vertientes convergen en una necesidad común: una herramienta que proporcione estructura biomecánica para el novato y análisis de datos de precisión para el avanzado, garantizando un funcionamiento fluido y autónomo que no dependa de factores externos de conectividad para el acceso a la información.

---

## 2. Propuesta de solución
Se propone el desarrollo de Overload, una aplicación móvil diseñada bajo un enfoque de utilidad real para el control del entrenamiento de fuerza. 

La aplicación permite estructurar rutinas basadas en sistemas probados (PPL, Full Body, Arnold Split, etc.) mediante un esquema de "slots" de ejercicios que garantiza un equilibrio biomecánico. Además de proporcionar una librería técnica de al menos 30 ejercicios, los cuales abarcarán los grupos musculares principales (pecho, espalda, hombros, bíceps y tríceps) con descripciones y soporte gráfico que elimina la incertidumbre sobre la ejecución. 

La solución centraliza el historial y ofrece una interfaz de ejecución dinámica que prioriza el registro de las marcas (peso, repeticiones, cercanía al fallo) de cada ejercicio durante la sesión y muestra referencias inmediatas de la sesión anterior para fomentar la progresión constante. Integra un sistema de análisis que transforma los datos crudos en métricas de volumen total y efectivo para evaluar el estímulo recibido.

Se consume la API de ExerciseDB para obtener los metadatos técnicos (instrucciones y biomecánica), mientras que el material visual se apoya en una biblioteca interna de archivos WebP optimizados. Esta configuración híbrida asegura que el usuario siempre tenga acceso instantáneo a la guía visual y a sus registros, eliminando tiempos de espera y garantizando el rendimiento de la aplicación en cualquier circunstancia.

---

## 3. Stack Tecnológico
* **Lenguaje de programación:** Kotlin, aprovechando sus capacidades de seguridad de nulos y concisión para el desarrollo nativo en Android.
* **Framework de interfaz:** Jetpack Compose, utilizando un paradigma declarativo para construir una UI moderna y fluida bajo los lineamientos de Material Design 3.
* **Base de datos local:** Room Persistence Library, que actúa como una capa de abstracción sobre SQLite. Esta permite una arquitectura *offline-first*, garantizando que el usuario pueda registrar sus series en entornos sin conexión con persistencia inmediata de datos.
* **Backend y Sincronización:** Uso de Firebase como Backend as a Service (BaaS).
    * **Firebase Authentication:** Gestión de la identidad y seguridad de las cuentas de usuario.
    * **Cloud Firestore:** Sincronización en la nube de los datos locales de Room para respaldo y recuperación entre dispositivos.
* **API Externa:** Se utiliza ExerciseDB como fuente para la obtención de metadatos técnicos, nombres oficiales y guías de ejecución. La integración se realiza mediante Ktor y OkHttp, permitiendo que la aplicación cuente con un catálogo estandarizado y profesional que sirve de base para la biblioteca local de la app.
* **Control de versiones:** GitHub bajo el flujo de trabajo Gitflow, separando el desarrollo de nuevas funcionalidades de la versión estable del proyecto.

---

## 4. Arquitectura de Software y Patrones de Diseño
Para garantizar la escalabilidad, mantenibilidad y el cumplimiento de los estándares de desarrollo oficiales de Google, Overload se rige bajo una arquitectura limpia y modular fundamentada en los siguientes pilares:

### 4.1. Patrón Arquitectónico de UI: MVVM (Model-View-ViewModel)
La capa de presentación está estrictamente separada de la lógica de negocio y de los datos:
* **View (Compose):** Se limita exclusivamente a renderizar la interfaz de usuario de forma reactiva a partir de un estado estructurado. No maneja lógica interna ni persistencia.
* **ViewModel:** Actúa como el puente del flujo de datos. Conserva el estado de la pantalla frente a cambios de configuración, expone dicho estado y delega los eventos del usuario hacia las capas de dominio correspondientes.
* **Model:** Compuesto por las entidades puras del negocio y la definición de las estructuras de datos.

### 4.2. Capa de Datos: Patrón Repository (Single Source of Truth)
Toda interacción con orígenes de datos se realiza a través de Repositorios específicos. Las pantallas nunca consumen Room o Ktor de forma directa. El repositorio abstrae el origen físico de la información y centraliza las reglas de la arquitectura *offline-first*, coordinando de manera transparente la base de datos local (Room), las peticiones HTTP remotas (Ktor) y la sincronización con el BaaS (Firebase).

### 4.3. Gestión de Estado: Patrón UI State
Cada pantalla expone un único flujo de estado inmutable (representado mediante `StateFlow` en Kotlin y consumido mediante `collectAsState()` en Compose). Este objeto de estado (`ScreenUiState`) es una clase de datos estricta que encapsula de forma segura las tres realidades posibles de la interfaz: `Loading`, `Success` (con la data estructurada cargada) y `Error` (con un mensaje controlado). Esto elimina estados inconsistentes en la UI.

### 4.4. Navegación: Jetpack Navigation 3
Se adopta de forma estricta el nuevo estándar nativo de **Navigation 3**. El flujo de navegación se gestiona de manera declarativa y con tipado seguro (*type-safe*), abstrayendo la pila de pantallas mediante destinos basados en objetos de configuración de Kotlin. Queda prohibido el uso de strings desestructurados como rutas (propios de Navigation 2.x), asegurando la validación del grafo de pantallas en tiempo de compilación.

### 5.5. Estructura Organizacional de Paquetes
El código fuente dentro del paquete principal (`com.pdm.overload`) se organiza rigurosamente bajo el principio de responsabilidad única. La estructura base de paquetes es:
* `data/`: Contiene los archivos locales y remotos de persistencia. Aloja sub paquetes para `local/` (tablas y DAO de Room), `remote/` (clientes de Ktor y llamadas a la API de ExerciseDB) y `repositories/` (las implementaciones del patrón repositorio).
* `domain/`: Contiene los modelos de negocio puros, clases de datos centrales de entrenamiento (Microciclos, Slots, Series) y los esquemas biomecánicos (Blueprints).
* `ui/`: Aloja toda la experiencia visual. Se subdivide en:
  * `screens/`: Funciones Composables organizadas por módulo (Acceso, Configuración, Entrenamiento, Análisis).
  * `viewmodels/`: Los ViewModels encargados de emitir el UI State para cada pantalla.
  * `routes/`: Clases de configuración y definición del grafo de navegación nativo de Navigation 3.
  * `theme/`: Archivos de configuración del sistema de diseño (Colores, Tipografías y Formas de Material Design 3).

---

# Flujo de la Aplicación y Lógica de Negocios - Overload

## 1. Módulo de Acceso e Inicialización
### Pantalla de Bienvenida / Login
* **Descripción:** Interfaz limpia que presenta la propuesta de valor y el logotipo de la aplicación.
* **Elementos:** Campos de texto para correo electrónico y contraseña, junto con botones de acción para "Iniciar Sesión" y "Crear Cuenta".
* **Lógica de Negocios:** La autenticación se gestiona a través de Firebase Auth. Si el token de sesión del usuario es válido y activo, la aplicación omite esta pantalla y navega directamente al Dashboard. Si es un usuario completamente nuevo, al registrarse se le redirige al Módulo de Configuración.

---

## 2. Módulo de Configuración (Arquitectura del Microciclo)
### 2.1. Pantalla de Selección de Sistema (Blueprints)
* **Descripción:** Galería de tarjetas que presentan los sistemas predefinidos de entrenamiento.
* **Opciones:** Tarjetas para esquemas probados como PPL, Full Body, Arnold Split y Upper/Lower.
* **Lógica de Negocios:** Al elegir un sistema, no se crea una "semana", sino que se carga una plantilla (Blueprint) que dicta las reglas biomecánicas. Por ejemplo, si se elige PPL, el sistema sabe que los días mínimos obligatorios son 3 (Push, Pull, Legs) y el máximo permitido para evitar el sobreentrenamiento son 6 días por microciclo.

### 2.2. Pantalla de Construcción del Microciclo
* **Descripción:** Vista donde el usuario define la longitud de su bloque de entrenamiento.
* **Interacción:** La interfaz carga automáticamente los días base obligatorios dictados por el Blueprint seleccionado (ej. Día 1: Push, Día 2: Pull, Día 3: Legs).
* **Flexibilidad:** El usuario dispone de un botón para "Añadir Día Extra". Puede agregar días adicionales (ej. Día 4: Push) hasta alcanzar el límite máximo de la plantilla. Cada día añadido es una instancia independiente en la base de datos.

### 2.3. Pantalla de Personalización de Rutina (Llenado de Slots)
* **Descripción:** Vista del "esqueleto" muscular de un día específico del microciclo.
* **Estructura:** Se presentan "slots" o ranuras predefinidas según el enfoque del día. Por ejemplo, un día "Push" mostrará slots agrupados por músculo (ej. 3 para Pecho, 2 para Hombro, 2 para Tríceps).
* **Interacción:** Cada slot muestra un botón de "Añadir" si está vacío, o el nombre del ejercicio si ya fue asignado. El usuario también debe definir en esta pantalla su objetivo de series (con un límite máximo de 5 para evitar volumen basura).

### 2.4. Pantalla de Librería de Ejercicios y Detalle
* **Descripción:** Catálogo técnico local (offline) que se despliega al presionar un slot vacío. La lista se filtra automáticamente para mostrar únicamente los ejercicios correspondientes al grupo muscular de dicho slot.
* **Elementos:** Buscador de texto, filtros por mecánica (Compuesto/Aislamiento) y tarjetas de resumen.
* **Detalle del Ejercicio (Modal/Ficha):** Al seleccionar un ejercicio, se abre una ficha con un GIF/WebP demostrativo, descripción técnica, músculos involucrados (principales y sinergistas) y los pasos de ejecución obtenidos de la API.

---

## 3. Módulo de Entrenamiento (Ejecución en Vivo)
### 3.1. Pantalla de Dashboard de Entrenamiento
* **Descripción:** Es la pantalla principal de la aplicación en el día a día. Determina en qué día del microciclo se encuentra el usuario y carga la lista de ejercicios correspondiente.
* **Elementos:** Tarjetas colapsables por cada ejercicio programado. Cada tarjeta actúa como una vista previa, mostrando el nombre y un panel de "Referencia Histórica" (Lo último realizado: Peso y Repeticiones de la sesión idéntica anterior).
* **Acción:** Botón flotante o estático de "Iniciar Sesión de Entrenamiento" que bloquea la rutina y habilita los campos de entrada.

### 3.2. Pantalla de Registro de Serie (Ficha Activa)
* **Descripción:** Al iniciar la sesión, las tarjetas se expanden permitiendo el ingreso de datos.
* **Campos de Entrada por Serie:**
  * **Peso:** Entrada numérica (kg/lb).
  * **Repeticiones:** Entrada numérica entera.
  * **RIR (Repeticiones en Reserva):** Un selector visual de 0 a 5 para medir la proximidad al fallo.
  * **Control de RIR (Toggle de Activación):** Un interruptor que permite al usuario desactivar la medición de RIR si prefiere realizar una serie de repeticiones fijas tradicionales.
* **Lógica de Flujo:** Al registrar y validar una serie, el foco de la interfaz salta automáticamente a la siguiente serie del mismo ejercicio. Al terminar todas las series de ese slot, la tarjeta se colapsa y se marca visualmente como completada. Cada serie almacena su estado de ponderación según el valor de RIR registrado.

---

## 4. Módulo de Análisis (Métricas)
### 4.1. Pantalla de Progreso por Ejercicio
* **Descripción:** Visualización analítica del rendimiento individual.
* **Elementos:** Gráfica de líneas que mapea la evolución temporal del ejercicio.
  * **Eje X:** Fecha cronológica de la sesión.
  * **Eje Y:** Volumen total calculado (Fórmula: Peso × Repeticiones × Series).

### 4.2. Pantalla de Análisis Muscular y Series Ponderadas
* **Descripción:** Visión macro del estímulo real recibido por cada grupo muscular mediante gráficas de barras o áreas.
* **Lógica de Negocios (Cálculo del Estímulo Real):** Para reflejar con precisión la ganancia de hipertrofia, el sistema diferencia el volumen crudo del volumen de estímulo real. El motor de análisis procesa cada serie aplicando la siguiente fórmula matemática:

  Volumen Efectivo Ponderado = Peso × Repeticiones × Factor_RIR

  El Factor_RIR se asigna de manera autónoma en la base de datos según el grado de esfuerzo guardado en el registro:

| Estado del RIR | Grado de Esfuerzo Estimado | Factor_RIR Aplicado |
| :--- | :--- | :--- |
| **RIR Desactivado (Fijo)** | Esfuerzo moderado estándar / Desconocido | **0.5** |
| **RIR 4 o 5** | Estímulo bajo (Muy alejado del fallo muscular) | **0.4** |
| **RIR 2 o 3** | Estímulo alto (Zona óptima de hipertrofia) | **0.8** |
| **RIR 0 o 1** | Estímulo máximo (Cercanía total al fallo / Alta intensidad) | **1.0** |

* **Propósito:** Demostrar si el estímulo general sobre un grupo muscular específico mantiene una tendencia de sobrecarga progresiva real a través de los microciclos. Esto evita que el usuario interprete erróneamente que está progresando si solo añade series lejanas al fallo (volumen basura), validando la eficacia científica del sistema elegido.

