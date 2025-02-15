# JLBerlin

## App

* Place your 'google-services.json' from your firebase console to 'app/google-services.json'
* Create file 'secrets.properties' and put your Google maps API key
```
MAPS_API_KEY=AIzaSyBbM-mVagxxwKPhTIuk8Omt6tlkQzDbqUg
```

## JLBerlin Server

### Build & Launch

1. ```./gradlew -p JLBerlinServer buildFatJar```
1. ```java -jar JLBerlinServer/build/libs/jlberlinserver.jar```