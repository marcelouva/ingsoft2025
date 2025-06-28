#!/bin/bash
find src -name "*.java" | entr -r mvn exec:java -Dexec.mainClass="com.is1.proyecto.App"
echo "📦 Código recompilado correctamente"