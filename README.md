# Gurru Finanzas

Aplicación móvil para Android desarrollada en **Kotlin** con **Jetpack Compose**, **Material 3**, **Room** y **WorkManager**, enfocada en el control financiero personal en pesos mexicanos (MXN) con almacenamiento 100% local.

## Funcionalidades principales

- **Agenda de ingresos recurrentes**: registra sueldo o ingresos con frecuencia semanal (eligiendo el día exacto, por ejemplo cada viernes), quincenal, mensual o única.
- **Agenda de deudas y recordatorios**: registra deudas con monto total, cuota y frecuencia de pago (semanal, quincenal o mensual), descuento de saldo al registrar pagos y notificaciones locales automáticas antes del vencimiento.
- **Control de gastos y detector de gastos hormiga**: clasifica tus gastos en *Hormiga*, *Fijo* y *Variable*, con atajos rápidos y cálculo de impacto mensual y anual de tus gastos hormiga.
- **Proyección de flujo de caja**: calcula tu balance mensual estimado y el **presupuesto diario seguro** hasta tu próximo día de cobro.
- **Regla 50/30/20 y alcancías**: seguimiento visual de Necesidades (50%), Deseos (30%) y Ahorro/Deudas (20%), más metas de ahorro con abonos.
- **Motor de sugerencias inteligentes**: alertas y recomendaciones automáticas basadas en tus ingresos, deudas, gastos hormiga y metas.

## Estructura del proyecto

- `app/src/main/java/com/gurru/finanzas/data/` — Entidades Room, DAOs, base de datos y repositorio.
- `app/src/main/java/com/gurru/finanzas/domain/` — Cálculo de recurrencias, proyector de flujo de caja y motor de asesoría financiera.
- `app/src/main/java/com/gurru/finanzas/notifications/` — Canal de notificaciones y `PaymentReminderWorker`.
- `app/src/main/java/com/gurru/finanzas/ui/` — Pantallas Compose (`Inicio`, `Gastos`, `Agenda`, `Plan`), tema Material 3 y `MainViewModel`.
- `app/src/test/java/com/gurru/finanzas/` — Pruebas unitarias de recurrencias y motor de sugerencias.

## Cómo compilar y ejecutar

1. Abre **Android Studio** y selecciona **Open** sobre esta carpeta (`Gurru Finanzas`).
2. Permite que Android Studio sincronice Gradle y descargue/configure el Android SDK (API 35).
3. Ejecuta la app en un emulador o dispositivo Android (Android 8.0 / API 26 o superior).
