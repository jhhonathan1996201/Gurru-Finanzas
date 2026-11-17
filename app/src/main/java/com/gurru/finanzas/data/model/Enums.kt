package com.gurru.finanzas.data.model

import java.time.DayOfWeek

enum class RecurrenceFrequency(val label: String) {
    SEMANAL("Semanal"),
    QUINCENAL("Quincenal"),
    MENSUAL("Mensual"),
    UNICO("Único")
}

enum class ExpenseType(val label: String, val description: String) {
    HORMIGA("Gasto Hormiga", "Pequeños gastos diarios que se acumulan"),
    FIJO("Gasto Fijo", "Pagos recurrentes necesarios cada mes"),
    VARIABLE("Gasto Variable", "Compras o salidas ocasionales")
}

enum class BudgetBucket(val label: String, val targetPercent: Double) {
    NECESIDAD("Necesidades (50%)", 0.50),
    DESEO("Deseos (30%)", 0.30),
    AHORRO_DEUDA("Ahorro y Deudas (20%)", 0.20)
}

enum class ExpenseCategory(val label: String, val defaultBucket: BudgetBucket) {
    COMIDA("Comida y Súper", BudgetBucket.NECESIDAD),
    HOGAR("Hogar y Renta", BudgetBucket.NECESIDAD),
    SERVICIOS("Servicios (Luz, Agua, Internet)", BudgetBucket.NECESIDAD),
    TRANSPORTE("Transporte y Gasolina", BudgetBucket.NECESIDAD),
    SALUD("Salud y Farmacia", BudgetBucket.NECESIDAD),
    CAFE_SNACKS("Café, Snacks y Antojos", BudgetBucket.DESEO),
    ENTRETENIMIENTO("Entretenimiento y Salidas", BudgetBucket.DESEO),
    SUSCRIPCIONES("Suscripciones y Apps", BudgetBucket.DESEO),
    COMPRAS("Compras Personales", BudgetBucket.DESEO),
    OTROS("Otros", BudgetBucket.DESEO)
}

fun dayOfWeekToSpanish(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

fun isoDayToSpanish(isoDay: Int?): String {
    if (isoDay == null || isoDay !in 1..7) return "Viernes"
    return dayOfWeekToSpanish(DayOfWeek.of(isoDay))
}
