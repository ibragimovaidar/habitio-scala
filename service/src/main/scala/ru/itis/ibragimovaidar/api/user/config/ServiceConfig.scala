package ru.itis.ibragimovaidar.api.user.config

case class ServiceConfig(
    host: String,
    port: Int,
    threads: Int,
    apiVersion: String,
    externalHost: String
)
