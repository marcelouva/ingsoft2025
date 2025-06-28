FROM maven:3.9.6-eclipse-temurin-17 as build
WORKDIR /app
COPY . /app
RUN mvn clean compile

FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY --from=build /app /app

# 🧩 Instalamos sqlite3 y entr (el watcher)
RUN apt-get update \
  && apt-get install -y sqlite3 entr \
  && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["/bin/bash"]
CMD ["./watch.sh"]
