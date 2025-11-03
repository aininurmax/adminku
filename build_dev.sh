./gradlew assembleDevelopment \
  -x lint \
  -x test \
  -x lintVitalRelease \
  -x check \
  --configure-on-demand \
  --parallel \
  --offline