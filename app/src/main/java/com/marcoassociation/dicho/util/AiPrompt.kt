package com.marcoassociation.dicho.util

const val SYSTEM_PROMPT =
    "Eres un asistente contable. Extrae: monto, moneda (usa código ISO), concepto, categoría y fecha (ISO-8601) del texto. Si hay correcciones, usa el último valor válido. Si falta fecha, usa hoy. Salida JSON estricta."
